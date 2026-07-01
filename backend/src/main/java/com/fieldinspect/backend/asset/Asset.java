package com.fieldinspect.backend.asset;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * An Asset is a piece of equipment a field technician inspects
 * (an HVAC unit, a water/energy meter, a sensor, a generator...).
 *
 * JPA annotations (JPA = Java's ORM standard, like Prisma):
 *   @Entity  -> "this class maps to a database table".
 *   @Table   -> the table name ("assets"); optional, defaults to the class name.
 *   @Id      -> the primary key.
 *   @GeneratedValue(IDENTITY) -> let the database auto-increment the id (like `@id @default(autoincrement())`).
 *
 * JPA requires:
 *   - a no-arg constructor (it uses reflection to build objects from DB rows).
 *   - fields with getters/setters (how it reads/writes each column).
 */
@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String type;              // HVAC, WATER_METER, ENERGY_METER, SENSOR, GENERATOR
    private String location;
    private String status;            // OK, NEEDS_INSPECTION, FAULT
    private Instant lastInspectedAt;  // nullable — some assets have never been inspected

    /** No-arg constructor required by JPA. `protected` nudges our code to use the one below. */
    protected Asset() {
    }

    /** Convenience constructor we use in the seeder and (later) when creating assets. */
    public Asset(String name, String type, String location, String status, Instant lastInspectedAt) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.status = status;
        this.lastInspectedAt = lastInspectedAt;
    }

    // --- Getters & setters: Spring reads these to serialize to JSON and to persist. ---

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getLastInspectedAt() {
        return lastInspectedAt;
    }

    public void setLastInspectedAt(Instant lastInspectedAt) {
        this.lastInspectedAt = lastInspectedAt;
    }
}
