package org.example.gameplay;

/** Tunable bleed DoT resolution (Joseph / Hemorrhage). */
public record BleedConfig(int tickCount, double tickIntervalSec, int tickDamageCap) {
    public static BleedConfig legacyDefaults() {
        return new BleedConfig(3, 0.35, 8);
    }
}
