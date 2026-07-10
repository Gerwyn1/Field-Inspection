package com.fieldinspect.backend;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Full-stack integration tests.
 *
 * @SpringBootTest        -> boot the REAL application: security filter chain,
 *                           JwtAuthFilter, DataSeeder, H2 — everything.
 * @AutoConfigureMockMvc  -> MockMvc fires HTTP requests at it IN-PROCESS
 *                           (no port, no network). It's Java's supertest.
 *
 * Deliberate choice: no security mocks. Every test that needs auth logs in
 * through the real /api/auth/login with the seeded demo users and sends the
 * real JWT — so these tests prove the whole chain, not pieces in isolation.
 *
 * The seeded database is shared by all tests in this class (Spring caches the
 * booted app between tests), so assertions avoid exact row counts — tests that
 * CREATE rows would break tests that COUNT them, depending on run order.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // -> tests run on H2 (see application-test.properties), not SQL Server
class ApiIntegrationTests {

    @Autowired
    private MockMvc mvc;

    // Jackson (JSON <-> objects). Our own instance — we only parse simple response
    // bodies here, no need for the app's configured one.
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** Log in as a seeded demo user and return a real JWT. */
    private String loginAs(String email) throws Exception {
        String body = """
                { "email": "%s", "password": "password123" }
                """.formatted(email); // """...""" is a text block — multiline strings, at last

        String json = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(json).get("token").asText();
    }

    /** Look up a seeded asset's id by name — never hardcode generated ids in tests. */
    private long assetIdByName(String token, String name) throws Exception {
        String json = mvc.perform(get("/api/assets")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        for (JsonNode asset : objectMapper.readTree(json)) {
            if (name.equals(asset.get("name").asText())) {
                return asset.get("id").asLong();
            }
        }
        throw new AssertionError("No seeded asset named " + name);
    }

    // --- Public vs protected ---

    @Test
    void pingIsPublic() throws Exception {
        mvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void protectedEndpointWithoutTokenIs401() throws Exception {
        mvc.perform(get("/api/assets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedTokenIs401() throws Exception {
        String token = loginAs("alan@fieldinspect.com");
        mvc.perform(get("/api/assets")
                .header("Authorization", "Bearer " + token + "X"))
                .andExpect(status().isUnauthorized());
    }

    // --- Login ---

    @Test
    void loginReturnsTokenAndUserInfo() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "email": "alan@fieldinspect.com", "password": "password123" }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value("Alan Tan"))
                .andExpect(jsonPath("$.role").value("TECHNICIAN"));
    }

    @Test
    void loginWithWrongPasswordIs401() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "email": "alan@fieldinspect.com", "password": "nope" }
                        """))
                .andExpect(status().isUnauthorized());
    }

    /** Regression test for the /error gotcha: a validation failure must be 400, not 401. */
    @Test
    void loginWithMalformedEmailIs400() throws Exception {
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "email": "not-an-email", "password": "password123" }
                        """))
                .andExpect(status().isBadRequest());
    }

    // --- Reading data with a token ---

    @Test
    void tokenGrantsAccessToAssets() throws Exception {
        String token = loginAs("alan@fieldinspect.com");
        mvc.perform(get("/api/assets")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("Rooftop HVAC Unit A")));
    }

    @Test
    void unknownAssetIdIs404() throws Exception {
        String token = loginAs("alan@fieldinspect.com");
        mvc.perform(get("/api/assets/99999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void usersEndpointNeverLeaksPasswordHashes() throws Exception {
        String token = loginAs("alan@fieldinspect.com");
        mvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").isNotEmpty())
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    // --- Submitting an inspection (the service-layer logic) ---

    @Test
    void technicianCanSubmitInspectionWithReadings() throws Exception {
        String token = loginAs("alan@fieldinspect.com");
        long generatorId = assetIdByName(token, "Backup Generator"); // seeded as never-inspected

        String responseJson = mvc.perform(post("/api/inspections")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "assetId": %d,
                          "status": "PASS",
                          "notes": "Generator started on first try.",
                          "readings": [
                            { "metric": "VOLTAGE",    "value": 230.4, "unit": "V" },
                            { "metric": "FUEL_LEVEL", "value": 82.0,  "unit": "%%" }
                          ]
                        }
                        """.formatted(generatorId)))
                .andExpect(status().isCreated())
                // the inspector was taken from the JWT, not from the request body:
                .andExpect(jsonPath("$.inspector.email").value("alan@fieldinspect.com"))
                .andExpect(jsonPath("$.status").value("PASS"))
                .andReturn().getResponse().getContentAsString();

        long inspectionId = objectMapper.readTree(responseJson).get("id").asLong();

        // Both readings were saved and linked to the new inspection...
        mvc.perform(get("/api/inspections/" + inspectionId + "/readings")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // ...and the business rule ran: the asset's summary fields were updated.
        mvc.perform(get("/api/assets/" + generatorId)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.lastInspectedAt").isNotEmpty());
    }

    @Test
    void inspectionForUnknownAssetIs404() throws Exception {
        String token = loginAs("alan@fieldinspect.com");
        mvc.perform(post("/api/inspections")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "assetId": 99999, "status": "PASS", "readings": [] }
                        """))
                .andExpect(status().isNotFound());
    }

    @Test
    void inspectionWithInvalidStatusIs400() throws Exception {
        String token = loginAs("alan@fieldinspect.com");
        mvc.perform(post("/api/inspections")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "assetId": 1, "status": "BANANA", "readings": [] }
                        """))
                .andExpect(status().isBadRequest()); // @Pattern rejects it before any code runs
    }

    // --- Role-based access ---

    @Test
    void technicianCannotCreateAssets() throws Exception {
        String token = loginAs("alan@fieldinspect.com"); // TECHNICIAN
        mvc.perform(post("/api/assets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "Sneaky Pump", "type": "PUMP", "location": "Block D" }
                        """))
                .andExpect(status().isForbidden()); // 403: known user, missing role
    }

    @Test
    void supervisorCanCreateAssets() throws Exception {
        String token = loginAs("siti@fieldinspect.com"); // SUPERVISOR
        mvc.perform(post("/api/assets")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        { "name": "Cooling Tower Pump", "type": "PUMP", "location": "Block D - Roof" }
                        """))
                .andExpect(status().isCreated())
                // the server, not the client, decides the starting state:
                .andExpect(jsonPath("$.status").value("NEEDS_INSPECTION"))
                .andExpect(jsonPath("$.lastInspectedAt").doesNotExist());
    }
}
