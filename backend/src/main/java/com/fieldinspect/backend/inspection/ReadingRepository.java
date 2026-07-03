package com.fieldinspect.backend.inspection;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingRepository extends JpaRepository<Reading, Long> {

    /** Derived query: SELECT * FROM readings WHERE inspection_id = ? */
    List<Reading> findByInspectionId(Long inspectionId);
}
