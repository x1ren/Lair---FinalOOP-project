package org.example.player;

/**
 * Defines the five survivors of THE LAIR.
 *
 * The enum stores the story-facing RPG stats so scenes can display
 * real character data without introducing extra model classes yet.
 */
public enum CharacterType {

    JOSEPH_JIMENEZ(
            "Joseph Jimenez",
            "Frontline Assault",
            "Heavy damage frontliner with burst output.",
            95, 90, 45,
            "Burst Drive",
            "Shockwave Punch",
            "Adrenal Lock"
    ),
    IBEN_ANOOS(
            "Iben Anoos",
            "Mobility Specialist",
            "Fast precision attacker with strong evasive control.",
            70, 85, 65,
            "Phase Dash",
            "Split Strike",
            "Evasion Pulse"
    ),
    ILDE_JAN_FIGUERAS(
            "Ilde Jan Figueras",
            "Defensive Controller",
            "Highest durability and battlefield control.",
            100, 70, 60,
            "Barrier Pulse",
            "Ground Lock",
            "Counter Surge"
    ),
    GAILE_AMOLONG(
            "Gaile Amolong",
            "Adaptive Tactical Fighter",
            "Balanced fighter who scales through aura control.",
            75, 80, 90,
            "Aura Thread",
            "Analyze Weakness",
            "Overclock"
    ),
    JAMUEL_BACUS(
            "Jamuel Bacus",
            "Energy Specialist",
            "Highest mana control, lowest physical output.",
            65, 60, 100,
            "Mana Pulse",
            "Chain Reaction",
            "Reserve Conversion"
    );

    public final String name;
    public final String title;
    public final String lore;

    private final int health;
    private final int logic;
    private final int wisdom;
    private final String skillOne;
    private final String skillTwo;
    private final String skillThree;

    public static final double BASE_SPEED = 260.0;
    public static final double BASE_JUMP  = -520.0;

    CharacterType(String name, String title, String lore,
                  int health, int logic, int wisdom,
                  String skillOne, String skillTwo, String skillThree) {
        this.name = name;
        this.title = title;
        this.lore = lore;
        this.health = health;
        this.logic = logic;
        this.wisdom = wisdom;
        this.skillOne = skillOne;
        this.skillTwo = skillTwo;
        this.skillThree = skillThree;
    }

    public int getHealth() {
        return health;
    }

    public int getLogic() {
        return logic;
    }

    public int getWisdom() {
        return wisdom;
    }

    public int getMana() {
        return wisdom * 2;
    }

    public int getBasicAttack() {
        return (int) Math.round(logic * 0.35);
    }

    public String getSkillOne() {
        return skillOne;
    }

    public String getSkillTwo() {
        return skillTwo;
    }

    public String getSkillThree() {
        return skillThree;
    }

    public String getSkillSummary() {
        return skillOne + "  |  " + skillTwo + "  |  " + skillThree;
    }

    public double getFinalSpeed(org.example.weapons.Weapon weapon) {
        return BASE_SPEED * weapon.getMoveSpeedModifier();
    }

    public double getFinalJump(org.example.weapons.Weapon weapon) {
        return BASE_JUMP * weapon.getJumpForceModifier();
    }
}
