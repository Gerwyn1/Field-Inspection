package com.fieldinspect.backend;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fieldinspect.backend.asset.Asset;
import com.fieldinspect.backend.asset.AssetRepository;
import com.fieldinspect.backend.inspection.Inspection;
import com.fieldinspect.backend.inspection.InspectionRepository;
import com.fieldinspect.backend.inspection.Reading;
import com.fieldinspect.backend.inspection.ReadingRepository;
import com.fieldinspect.backend.user.AppUser;
import com.fieldinspect.backend.user.AppUserRepository;

/**
 * Seeds demo data on startup. (Moved up from the asset package now that it seeds
 * every table, not just assets.)
 *
 * Seed ORDER matters: users and assets first, because inspections hold foreign
 * keys to both; readings last, because they point at inspections. save(...)
 * returns the saved entity WITH its database-generated id, so we hold on to the
 * returned objects and pass them into the next layer.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AssetRepository assets;
    private final AppUserRepository users;
    private final InspectionRepository inspections;
    private final ReadingRepository readings;

    public DataSeeder(AssetRepository assets, AppUserRepository users,
            InspectionRepository inspections, ReadingRepository readings) {
        this.assets = assets;
        this.users = users;
        this.inspections = inspections;
        this.readings = readings;
    }

    @Override
    public void run(String... args) {
        if (assets.count() > 0) {
            return; // already seeded — do nothing
        }

        Instant now = Instant.now();

        // --- Users ---
        AppUser alan = users.save(new AppUser("Alan Tan", "alan@fieldinspect.com", "TECHNICIAN"));
        AppUser siti = users.save(new AppUser("Siti Rahman", "siti@fieldinspect.com", "SUPERVISOR"));

        // --- Assets (same five as before, now kept in variables so inspections can link to them) ---
        Asset hvac = assets.save(new Asset("Rooftop HVAC Unit A", "HVAC", "Block A - Rooftop", "OK",
                now.minus(20, ChronoUnit.DAYS)));
        Asset waterMeter = assets.save(new Asset("Chilled Water Meter", "WATER_METER", "Block B - Basement",
                "NEEDS_INSPECTION", now.minus(95, ChronoUnit.DAYS)));
        Asset energyMeter = assets.save(new Asset("Main Energy Meter", "ENERGY_METER", "Substation 1", "OK",
                now.minus(10, ChronoUnit.DAYS)));
        Asset sensor = assets.save(new Asset("Temp/Humidity Sensor 3F", "SENSOR", "Block A - Level 3", "FAULT",
                now.minus(2, ChronoUnit.DAYS)));
        assets.save(new Asset("Backup Generator", "GENERATOR", "Block C - Ground", "OK",
                null)); // never inspected -> deliberately gets no inspection rows

        // --- Inspections (performedAt lines up with each asset's lastInspectedAt) ---
        Inspection hvacCheck = inspections.save(new Inspection(hvac, alan, "PASS",
                "Filters cleaned, airflow nominal.", now.minus(20, ChronoUnit.DAYS)));
        Inspection meterCheck = inspections.save(new Inspection(waterMeter, alan, "NEEDS_FOLLOW_UP",
                "Slight leak at the coupling; monitor on next visit.", now.minus(95, ChronoUnit.DAYS)));
        Inspection energyCheck = inspections.save(new Inspection(energyMeter, siti, "PASS",
                "All readings within expected range.", now.minus(10, ChronoUnit.DAYS)));
        Inspection sensorCheck = inspections.save(new Inspection(sensor, alan, "FAIL",
                "Humidity stuck at 88% — sensor likely faulty, raise work order.", now.minus(2, ChronoUnit.DAYS)));

        // --- Readings (the measurements captured during each inspection) ---
        readings.save(new Reading(hvacCheck, "TEMPERATURE", 24.5, "°C", now.minus(20, ChronoUnit.DAYS)));
        readings.save(new Reading(hvacCheck, "VIBRATION", 1.8, "mm/s", now.minus(20, ChronoUnit.DAYS)));
        readings.save(new Reading(meterCheck, "FLOW_RATE", 12.4, "L/min", now.minus(95, ChronoUnit.DAYS)));
        readings.save(new Reading(energyCheck, "VOLTAGE", 230.1, "V", now.minus(10, ChronoUnit.DAYS)));
        readings.save(new Reading(energyCheck, "CURRENT", 42.7, "A", now.minus(10, ChronoUnit.DAYS)));
        readings.save(new Reading(sensorCheck, "TEMPERATURE", 31.2, "°C", now.minus(2, ChronoUnit.DAYS)));
        readings.save(new Reading(sensorCheck, "HUMIDITY", 88.0, "%", now.minus(2, ChronoUnit.DAYS)));
    }
}
