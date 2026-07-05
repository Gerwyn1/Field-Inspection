package com.fieldinspect.backend.asset;

import jakarta.validation.constraints.NotBlank;

/**
 * Body for POST /api/assets. No status/lastInspectedAt — a brand-new asset always
 * starts as NEEDS_INSPECTION and never-inspected; the server decides that, not
 * the client.
 */
public record CreateAssetRequest(
        @NotBlank String name,
        @NotBlank String type,
        @NotBlank String location) {
}
