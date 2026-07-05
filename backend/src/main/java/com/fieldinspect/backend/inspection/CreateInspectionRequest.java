package com.fieldinspect.backend.inspection;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * The JSON body the mobile app will send after a field visit — the inspection
 * and its measurements in ONE request, e.g.:
 *
 *   {
 *     "assetId": 5,
 *     "status": "PASS",
 *     "notes": "Generator started fine.",
 *     "readings": [ { "metric": "VOLTAGE", "value": 230.4, "unit": "V" } ]
 *   }
 *
 * Notice who is NOT in here: the inspector. The server takes that from the JWT —
 * never trust the client to say who performed the work.
 *
 * Validation notes:
 *   @Pattern           -> status must be one of our three values, anything else -> 400.
 *   List<@Valid ...>   -> validate every element of the list, not just the list itself.
 */
public record CreateInspectionRequest(
        @NotNull Long assetId,
        @NotBlank @Pattern(regexp = "PASS|FAIL|NEEDS_FOLLOW_UP") String status,
        String notes,
        @NotNull List<@Valid NewReading> readings) {

    /** Records can nest — this one only exists as part of a CreateInspectionRequest. */
    public record NewReading(
            @NotBlank String metric,
            @NotNull Double value,
            @NotBlank String unit) {
    }
}