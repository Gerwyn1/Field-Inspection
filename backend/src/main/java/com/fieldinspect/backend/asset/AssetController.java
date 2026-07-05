package com.fieldinspect.backend.asset;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

/**
 * REST endpoints for assets.
 *
 * @RequestMapping("/api/assets") sets the base path for every method here.
 *
 * Dependency injection:
 *   We declare `AssetRepository` as a constructor parameter and Spring automatically
 *   passes in the generated repository at startup. This is the same idea as NestJS's
 *   constructor injection — we never call `new AssetController(...)` ourselves.
 */
@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetRepository repository;

    public AssetController(AssetRepository repository) {
        this.repository = repository;
    }   

    /** GET /api/assets -> returns every asset as a JSON array. */
    @GetMapping
    public List<Asset> all() {
        return repository.findAll();
    }

    /** GET /api/assets/3 -> one asset, or a 404 if the id doesn't exist. */
    @GetMapping("/{id}")
    public Asset one(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No asset with id " + id));
    }

    /**
     * POST /api/assets -> register a new asset.
     *
     * NEW CONCEPT — @PreAuthorize: role-based access on a single method.
     * Technicians submit inspections, but only supervisors manage the asset
     * registry. The filter set "ROLE_SUPERVISOR" as the user's authority (from
     * the JWT), and hasRole('SUPERVISOR') checks exactly that; anyone else gets
     * a 403 Forbidden ("I know who you are, and no").
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPERVISOR')")
    @ResponseStatus(HttpStatus.CREATED)
    public Asset create(@Valid @RequestBody CreateAssetRequest body) {
        return repository.save(new Asset(body.name(), body.type(), body.location(),
                "NEEDS_INSPECTION", null)); // new assets always start un-inspected
    }
}
