package com.fieldinspect.backend.inspection;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A single measured value captured during an inspection — one temperature,
 * one voltage, one humidity percentage. In Phase 4 these will stream in from a
 * BLE sensor; for now the seeder writes them by hand.
 *
 * Note the @Column rename: VALUE is a reserved word in H2 (like USER was for the
 * users table), so the Java field stays `value` but the column is "reading_value".
 */
@Entity
@Table(name = "readings")
public class Reading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inspection_id")
    private Inspection inspection;

    private String metric;      // TEMPERATURE, HUMIDITY, VOLTAGE, CURRENT, FLOW_RATE, VIBRATION

    @Column(name = "reading_value")
    private double value;

    private String unit;        // °C, %, V, A, L/min, mm/s
    private Instant recordedAt;

    /** No-arg constructor required by JPA. */
    protected Reading() {
    }

    public Reading(Inspection inspection, String metric, double value, String unit, Instant recordedAt) {
        this.inspection = inspection;
        this.metric = metric;
        this.value = value;
        this.unit = unit;
        this.recordedAt = recordedAt;
    }

    public Long getId() {
        return id;
    }

    public Inspection getInspection() {
        return inspection;
    }

    public void setInspection(Inspection inspection) {
        this.inspection = inspection;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }
}
