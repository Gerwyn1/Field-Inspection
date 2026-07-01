package com.fieldinspect.backend.asset;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * A Repository is our data-access layer.
 *
 * Here's the magic: we only declare an *interface* — we write NO implementation.
 * At startup Spring Data JPA generates the concrete class for us, giving us a full
 * set of CRUD methods for free:
 *
 *   findAll()      -> SELECT * FROM assets
 *   findById(id)   -> SELECT * FROM assets WHERE id = ?
 *   save(asset)    -> INSERT or UPDATE
 *   deleteById(id) -> DELETE
 *   count()        -> SELECT COUNT(*)
 *
 * JpaRepository<Asset, Long> means: "manage Asset entities whose id type is Long".
 * (Roughly the equivalent of `prisma.asset` giving you .findMany/.create/etc.)
 */
public interface AssetRepository extends JpaRepository<Asset, Long> {
}
