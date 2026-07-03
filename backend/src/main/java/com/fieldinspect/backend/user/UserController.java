package com.fieldinspect.backend.user;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Same shape as AssetController: constructor injection + one list endpoint.
 * (Once JWT lands in sub-step 4 this endpoint will require authentication.)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final AppUserRepository repository;

    public UserController(AppUserRepository repository) {
        this.repository = repository;
    }

    /** GET /api/users -> every user as a JSON array. */
    @GetMapping
    public List<AppUser> all() {
        return repository.findAll();
    }
}
