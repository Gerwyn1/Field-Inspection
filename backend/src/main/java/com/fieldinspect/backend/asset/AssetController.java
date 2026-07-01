package com.fieldinspect.backend.asset;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
