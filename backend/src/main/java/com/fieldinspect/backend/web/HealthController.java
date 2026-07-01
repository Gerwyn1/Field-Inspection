package com.fieldinspect.backend.web;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple health-check endpoint.
 *
 * In Express this would be:
 *   app.get('/api/ping', (req, res) => res.json({ status: 'ok', ... }));
 *
 * Annotations explained:
 *   @RestController  -> this class handles HTTP requests and returns data (JSON), not HTML pages.
 *   @RequestMapping  -> a shared URL prefix for every method in this class ("/api").
 *   @GetMapping      -> handle HTTP GET requests at this sub-path ("/ping" -> "/api/ping").
 *
 * Returning a Map (or any object) makes Spring automatically convert it to JSON.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "status", "ok",
                "service", "field-inspection-backend",
                "time", Instant.now().toString());
    }
}
