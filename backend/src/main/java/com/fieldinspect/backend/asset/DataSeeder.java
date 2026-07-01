package com.fieldinspect.backend.asset;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds a few demo assets on startup so the API has something to return.
 *
 * @Component            -> Spring creates and manages one instance of this class.
 * CommandLineRunner     -> its run(...) method executes once, right after the app boots.
 *                          (This is our equivalent of a `prisma seed` script.)
 *
 * We guard with count() so we only seed when the table is empty.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final AssetRepository assets;

    public DataSeeder(AssetRepository assets) {
        this.assets = assets;
    }

    @Override
    public void run(String... args) {
        if (assets.count() > 0) {
            return; // already seeded — do nothing
        }

        Instant now = Instant.now();

        assets.save(new Asset("Rooftop HVAC Unit A", "HVAC", "Block A - Rooftop", "OK",
                now.minus(20, ChronoUnit.DAYS)));
        assets.save(new Asset("Chilled Water Meter", "WATER_METER", "Block B - Basement", "NEEDS_INSPECTION",
                now.minus(95, ChronoUnit.DAYS)));
        assets.save(new Asset("Main Energy Meter", "ENERGY_METER", "Substation 1", "OK",
                now.minus(10, ChronoUnit.DAYS)));
        assets.save(new Asset("Temp/Humidity Sensor 3F", "SENSOR", "Block A - Level 3", "FAULT",
                now.minus(2, ChronoUnit.DAYS)));
        assets.save(new Asset("Backup Generator", "GENERATOR", "Block C - Ground", "OK",
                null)); // never inspected
    }
}
