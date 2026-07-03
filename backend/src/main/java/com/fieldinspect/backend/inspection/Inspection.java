package com.fieldinspect.backend.inspection;

import java.time.Instant;

import com.fieldinspect.backend.asset.Asset;
import com.fieldinspect.backend.user.AppUser;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * One visit by one technician to one asset: "Alan checked the rooftop HVAC on
 * Tuesday, result PASS, here are his notes."
 *
 * NEW CONCEPT — relationships:
 *   @ManyToOne  -> many inspections point at one asset (and one user). In the database
 *                  this is just a foreign-key column; in Prisma terms it's:
 *                      asset  Asset  @relation(fields: [assetId], references: [id])
 *   @JoinColumn -> names that foreign-key column ("asset_id").
 *
 * We deliberately do NOT add the mirror-image `@OneToMany List<Inspection>` on Asset.
 * A two-way link creates a JSON cycle (asset -> inspections -> asset -> ...) that
 * blows up serialization; one-way is simpler and all we need. To ask "which
 * inspections does asset 3 have?" we query the repository instead.
 *
 * @ManyToOne loads its target eagerly by default (a SQL JOIN), so the JSON for an
 * inspection embeds the full asset and inspector objects — handy for now; when the
 * payloads grow we'll introduce DTOs to trim them.
 */
@Entity
@Table(name = "inspections")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne
    @JoinColumn(name = "inspector_id")
    private AppUser inspector;

    private String status;      // PASS, FAIL, NEEDS_FOLLOW_UP
    private String notes;
    private Instant performedAt;

    /** No-arg constructor required by JPA. */
    protected Inspection() {
    }

    public Inspection(Asset asset, AppUser inspector, String status, String notes, Instant performedAt) {
        this.asset = asset;
        this.inspector = inspector;
        this.status = status;
        this.notes = notes;
        this.performedAt = performedAt;
    }

    public Long getId() {
        return id;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public AppUser getInspector() {
        return inspector;
    }

    public void setInspector(AppUser inspector) {
        this.inspector = inspector;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(Instant performedAt) {
        this.performedAt = performedAt;
    }
}
