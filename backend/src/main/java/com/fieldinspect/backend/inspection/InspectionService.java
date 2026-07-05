package com.fieldinspect.backend.inspection;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fieldinspect.backend.asset.Asset;
import com.fieldinspect.backend.asset.AssetRepository;
import com.fieldinspect.backend.user.AppUser;
import com.fieldinspect.backend.user.AppUserRepository;

/**
 * NEW CONCEPT — the service layer. Controllers stay thin (parse request, call
 * service, return result); anything that is a business RULE lives here. Same
 * split as NestJS controller/service classes.
 *
 * Why now? Creating an inspection is the first operation that touches several
 * tables AND has a rule attached ("submitting an inspection updates the asset's
 * status"). That doesn't belong in an HTTP handler.
 */
@Service
public class InspectionService {

    private final InspectionRepository inspections;
    private final ReadingRepository readings;
    private final AssetRepository assets;
    private final AppUserRepository users;

    public InspectionService(InspectionRepository inspections, ReadingRepository readings,
            AssetRepository assets, AppUserRepository users) {
        this.inspections = inspections;
        this.readings = readings;
        this.assets = assets;
        this.users = users;
    }

    /**
     * NEW CONCEPT — @Transactional: every DB write in this method commits together
     * or not at all (like prisma.$transaction). If saving the 3rd reading blew up,
     * we would NOT be left with a half-saved inspection.
     */
    @Transactional
    public Inspection create(CreateInspectionRequest request, String inspectorEmail) {
        Asset asset = assets.findById(request.assetId())
                // ResponseStatusException: throw an exception that IS an HTTP response.
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No asset with id " + request.assetId()));

        // The email comes from the verified JWT, so this lookup can only fail if the
        // user was deleted after their token was issued.
        AppUser inspector = users.findByEmail(inspectorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Instant now = Instant.now();

        Inspection inspection = inspections.save(
                new Inspection(asset, inspector, request.status(), request.notes(), now));

        for (CreateInspectionRequest.NewReading r : request.readings()) {
            readings.save(new Reading(inspection, r.metric(), r.value(), r.unit(), now));
        }

        // Business rule: an inspection updates the asset's at-a-glance summary.
        // (switch EXPRESSION — modern Java: it returns a value, no fall-through.)
        asset.setLastInspectedAt(now);
        asset.setStatus(switch (request.status()) {
            case "PASS" -> "OK";
            case "FAIL" -> "FAULT";
            default -> "NEEDS_INSPECTION"; // NEEDS_FOLLOW_UP
        });
        assets.save(asset);

        return inspection;
    }
}
