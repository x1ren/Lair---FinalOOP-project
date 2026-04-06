package org.example.player;

import java.util.EnumMap;
import java.util.Map;

/**
 * Resolves {@link CharacterCombatProfile} per survivor. Tuning is centralized here (and optional resource overrides later).
 */
public final class CharacterCombatProfiles {

    private static final Map<CharacterType, CharacterCombatProfile> PROFILES = new EnumMap<>(CharacterType.class);

    static {
        PROFILES.put(CharacterType.JOSEPH_JIMENEZ, new CharacterCombatProfile(
                5.0, 5.0, 5.0, 2, 2, 5.0, 1,
                3.0, 3.0,
                24.0 / 130.0,
                3, 0.35, 8,
                1.8, 4, 0.65, 1.08,
                1.35, 0.55, 1.12,
                0, 1.0,
                0.55,
                200
        ));
        PROFILES.put(CharacterType.IBEN_ANOOS, new CharacterCombatProfile(
                5.0, 5.0, 5.0, 2, 2, 5.0, 1,
                3.0, 3.0,
                0, 3, 0.35, 8,
                1.8, 4, 0.65, 1.08,
                1.35, 0.55, 1.12,
                0, 1.0,
                0.55,
                200
        ));
        PROFILES.put(CharacterType.ILDE_JAN_FIGUERAS, new CharacterCombatProfile(
                5.0, 5.0, 5.0, 2, 2, 5.0, 1,
                3.0, 3.0,
                0, 3, 0.35, 8,
                1.8, 4, 0.65, 1.08,
                1.35, 0.55, 1.15,
                0, 1.0,
                0.55,
                200
        ));
        PROFILES.put(CharacterType.GAILE_AMOLONG, new CharacterCombatProfile(
                5.0, 5.0, 5.0, 2, 2, 5.0, 1,
                3.0, 3.0,
                0, 3, 0.35, 8,
                1.8, 4, 0.65, 1.08,
                1.35, 0.55, 1.12,
                20, 0.88,
                0.55,
                220
        ));
        PROFILES.put(CharacterType.JAMUEL_BACUS, new CharacterCombatProfile(
                5.0, 5.0, 5.0, 2, 2, 5.0, 1,
                2.75, 3.25,
                0, 3, 0.35, 8,
                1.8, 4, 0.65, 1.08,
                1.35, 0.55, 1.12,
                0, 1.0,
                0.55,
                200
        ));
    }

    private CharacterCombatProfiles() {
    }

    public static CharacterCombatProfile forCharacter(CharacterType type) {
        return PROFILES.get(type);
    }
}
