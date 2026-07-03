package com.fieldinspect.backend.inspection;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for inspections and their readings.
 *
 * Two new annotations, both direct Express equivalents:
 *   @RequestParam -> a ?query=param   (Express: req.query.assetId)
 *   @PathVariable -> a /{segment}     (Express: req.params.id)
 */
@RestController
@RequestMapping("/api/inspections")
public class InspectionController {

    private final InspectionRepository inspections;
    private final ReadingRepository readings;

    public InspectionController(InspectionRepository inspections, ReadingRepository readings) {
        this.inspections = inspections;
        this.readings = readings;
    }

    /**
     * GET /api/inspections            -> every inspection
     * GET /api/inspections?assetId=2  -> only that asset's inspections
     *
     * required = false makes the param optional; it arrives as null when absent.
     */
    @GetMapping
    public List<Inspection> all(@RequestParam(required = false) Long assetId) {
        if (assetId != null) {
            return inspections.findByAssetId(assetId);
        }
        return inspections.findAll();
    }

    /** GET /api/inspections/4/readings -> the measurements captured during inspection 4. */
    @GetMapping("/{id}/readings")
    public List<Reading> readingsFor(@PathVariable Long id) {
        return readings.findByInspectionId(id);
    }
}
