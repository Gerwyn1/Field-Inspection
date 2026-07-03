package com.fieldinspect.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Same pattern as AssetRepository: declare the interface, Spring Data JPA
 * generates the implementation (findAll/findById/save/delete/count) at startup.
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
}
