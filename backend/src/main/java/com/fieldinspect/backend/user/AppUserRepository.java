package com.fieldinspect.backend.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Same pattern as AssetRepository: declare the interface, Spring Data JPA
 * generates the implementation (findAll/findById/save/delete/count) at startup.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    /**
     * Derived query: SELECT * FROM users WHERE email = ?
     * Optional<AppUser> is Java's "maybe there's no row" wrapper — like a return
     * type of `AppUser | undefined` in TS, but you're forced to handle the
     * missing case explicitly.
     */
    Optional<AppUser> findByEmail(String email);
}
