package com.fieldinspect.backend.inspection;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    /**
     * NEW CONCEPT — derived query method: Spring Data parses the METHOD NAME and
     * generates the query. "findByAssetId" -> follow the `asset` relation, match
     * its `id` -> SELECT * FROM inspections WHERE asset_id = ?
     * (Prisma equivalent: prisma.inspection.findMany({ where: { assetId } }))
     */
    List<Inspection> findByAssetId(Long assetId);
}
