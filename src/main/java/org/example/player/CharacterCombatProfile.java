package org.example.player;

/**
 * Data-driven combat tuning for a survivor. Unused fields keep neutral defaults per character.
 */
public record CharacterCombatProfile(
        double hemorrhageDurationSec,
        double suppressDurationSec,
        double overdriveDurationSec,
        int overloadChargeBudget,
        int overloadPelletMultiplier,
        double focusDurationSec,
        int focusShotBudget,
        /** Jamuel: at start of Focus window (high remaining time). */
        double focusDamageMultiplierEarly,
        /** Jamuel: at end of Focus window (low remaining time) — rewards patient shots. */
        double focusDamageMultiplierLate,
        /** Joseph: bleed DoT total per hit ≈ shotDamage * this (legacy ~0.185 for dmg 130 → 24). */
        double hemorrhageBleedPercentOfShotDamage,
        int hemorrhageBleedTickCount,
        double hemorrhageBleedTickIntervalSec,
        int hemorrhageBleedTickDamageCap,
        double suppressSlowDurationSec,
        int suppressSlowDiminishAfterStacks,
        double suppressSlowDiminishFactor,
        double suppressDamageVsSlowedMultiplier,
        double overdriveMoveMultiplier,
        double overdriveFireRateCooldownMultiplier,
        double overdriveBonusDamageMultiplier,
        int tankMaxHpBonus,
        double overloadDamageTakenMultiplier,
        double enemySlowMoveFactor,
        int profileBaseMaxHp
) {
}
