package org.example.gameplay;

/**
 * Lightweight adaptation: recent status pressure influences brief enemy surges.
 */
public final class EnemyTuningState {

    private static final double BLEED_PRESSURE_DECAY_PER_SEC = 1.2;
    private static final double SLOW_PRESSURE_DECAY_PER_SEC = 1.4;
    private static final double SLOW_PRESSURE_ADRENALINE_THRESHOLD = 3.5;
    private static final double BLEED_PRESSURE_FERVOR_THRESHOLD = 5.0;

    private double bleedPressure;
    private double slowPressure;
    private double adrenalineTimer;
    private double fervorTimer;

    public void update(double dt) {
        bleedPressure = Math.max(0, bleedPressure - BLEED_PRESSURE_DECAY_PER_SEC * dt);
        slowPressure = Math.max(0, slowPressure - SLOW_PRESSURE_DECAY_PER_SEC * dt);
        adrenalineTimer = Math.max(0, adrenalineTimer - dt);
        fervorTimer = Math.max(0, fervorTimer - dt);
    }

    public void onBleedApplied() {
        bleedPressure += 2.0;
        if (bleedPressure >= BLEED_PRESSURE_FERVOR_THRESHOLD) {
            fervorTimer = Math.max(fervorTimer, 1.6);
        }
    }

    public void onSlowApplied() {
        slowPressure += 1.2;
        if (slowPressure >= SLOW_PRESSURE_ADRENALINE_THRESHOLD) {
            adrenalineTimer = Math.max(adrenalineTimer, 2.0);
            slowPressure *= 0.35;
        }
    }

    /** Move speed multiplier from adaptation (1.0 = none). */
    public double moveMultiplier() {
        return adrenalineTimer > 0 ? 1.10 : 1.0;
    }

    /** Multiplier on attack cooldown (lower = faster attacks). */
    public double attackCooldownMultiplier() {
        return fervorTimer > 0 ? 0.88 : 1.0;
    }

    public boolean isAdrenaline() {
        return adrenalineTimer > 0;
    }

    public boolean isFervor() {
        return fervorTimer > 0;
    }
}
