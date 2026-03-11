package org.example.player;

/**
 * ENCAPSULATION
 *
 * Defines the 5 playable characters from THE LAIR.
 * All share identical base stats — the weapon chosen
 * applies multipliers on top of these values.
 */
public enum CharacterType {

    JOSEPH_JIMENEZ(
            "Joseph Jimenez",
            "The Tactician",
            "Calculated and composed. Joseph thinks before he acts.\nHe was the one who wanted to stay and finish the project."
    ),
    IBEN_ANOOS(
            "Iben Anoos",
            "The Silent One",
            "Few words, sharp instincts. Iben notices what others miss.\nHe sensed something wrong before the meteor hit."
    ),
    ILDE_JAN_FIGUERAS(
            "Ilde Jan Figueras",
            "The Swift",
            "Always moving, always first. Ilde acts on instinct.\nHe was already running when the others were still standing."
    ),
    GAILE_AMOLONG(
            "Gaile Amolong",
            "The Resilient",
            "Takes the hit and keeps going. Gaile doesn't quit.\nHe was the last one to fall unconscious."
    ),
    JAMUEL_BACUS(
            "Jamuel Bacus",
            "The Relentless",
            "He doesn't stop. Not for exhaustion, not for pain.\nJamuel was already on his feet when the others woke."
    );

    public final String name;
    public final String title;
    public final String lore;

    // Base stats — SAME for all characters
    public static final int    BASE_HEALTH = 10;
    public static final double BASE_SPEED  = 260.0;
    public static final double BASE_JUMP   = -520.0;

    CharacterType(String name, String title, String lore) {
        this.name  = name;
        this.title = title;
        this.lore  = lore;
    }

    public double getFinalSpeed(org.example.weapons.Weapon weapon) {
        return BASE_SPEED * weapon.getMoveSpeedModifier();
    }

    public double getFinalJump(org.example.weapons.Weapon weapon) {
        return BASE_JUMP * weapon.getJumpForceModifier();
    }
}