package org.example.gameplay;

/** Global player-side scaling for late-stage pacing. */
public final class CombatScaling {

    private static final double DAMAGE_PER_STAGE = 0.03;

    private CombatScaling() {
    }

    public static double playerDamageStageMultiplier(int stageIndex) {
        return 1.0 + stageIndex * DAMAGE_PER_STAGE;
    }
}
