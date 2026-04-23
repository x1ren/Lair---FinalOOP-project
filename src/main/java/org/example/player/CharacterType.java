package org.example.player;

import org.example.weapons.Weapon;
import org.example.weapons.WeaponCatalog;
import org.example.weapons.WeaponType;

/**
 * Defines the five survivors of THE LAIR.
 *
 * Each survivor now carries one fixed weapon loadout instead of choosing from
 * a shared armory. This keeps the data simple and lets the scenes and
 * gameplay read one source of truth for HP, damage, movement speed, and skill.
 */
public enum CharacterType {

    JOSEPH_JIMENEZ(
            "Joseph Jimenez",
            "Balanced Fighter",
            "All-rounder starter pick with an assault rifle and a bleed-focused ultimate.",
            WeaponType.ASSAULT_RIFLE,
            200,
            130,
            100,
            "Hemorrhage",
            6.0,
            "character.joseph",
            false
    ),
    IBEN_ANOOS(
            "Iben Anoos",
            "Glass Cannon",
            "High-output LMG user whose suppressive fire slows enemies during the ultimate.",
            WeaponType.LMG,
            200,
            110,
            70,
            "Suppress",
            6.0,
            "character.iben",
            false
    ),
    ILDE_JAN_FIGUERAS(
            "Ilde Jan Figueras",
            "Bruiser",
            "Heavy hitter with an SMG and the fastest movement speed in the roster.",
            WeaponType.SMG,
            200,
            80,
            130,
            "Overdrive",
            5.0,
            "character.ilde",
            false
    ),
    GAILE_AMOLONG(
            "Gaille Amolong",
            "Tank",
            "Frontline shotgun user with durable stats and an overload burst ultimate.",
            WeaponType.SHOTGUN,
            200,
            180,
            80,
            "Overload",
            6.0,
            "character.gaille",
            true
    ),
    JAMUEL_BACUS(
            "Jamuel Bacus",
            "Skill Specialist",
            "Precision sniper built around tactical skill timing and massive burst windows.",
            WeaponType.SNIPER,
            200,
            250,
            65,
            "Focus",
            6.0,
            "character.jamuel",
            false
    );

    public static final double BASE_MOVE_SPEED = 260.0;
    public static final double BASE_JUMP = -520.0;

    private final String name;
    private final String title;
    private final String lore;
    private final WeaponType assignedWeaponType;
    private final int health;
    private final int damage;
    private final int movementSpeed;
    private final String skillName;
    private final double skillCooldown;
    private final String spriteAssetId;
    private final boolean femaleVoice;

    CharacterType(String name, String title, String lore,
                  WeaponType assignedWeaponType,
                  int health, int damage, int movementSpeed,
                  String skillName, double skillCooldown,
                  String spriteAssetId, boolean femaleVoice) {
        this.name = name;
        this.title = title;
        this.lore = lore;
        this.assignedWeaponType = assignedWeaponType;
        this.health = health;
        this.damage = damage;
        this.movementSpeed = movementSpeed;
        this.skillName = skillName;
        this.skillCooldown = skillCooldown;
        this.spriteAssetId = spriteAssetId;
        this.femaleVoice = femaleVoice;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getLore() {
        return lore;
    }

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public int getMovementSpeed() {
        return movementSpeed;
    }

    public double getMovementSpeedPx() {
        return BASE_MOVE_SPEED * movementSpeed / 100.0;
    }

    public String getSkillName() {
        return skillName;
    }

    /** One-line HUD hint for what the skill does while active or primed. */
    public String getSkillEffectSummary() {
        return switch (this) {
            case JOSEPH_JIMENEZ -> "While active: hits apply bleed damage over time.";
            case IBEN_ANOOS -> "While active: hits slow enemies; bonus vs slowed targets.";
            case ILDE_JAN_FIGUERAS -> "While active: faster move, fire rate, and bonus damage.";
            case GAILE_AMOLONG -> "Next shotgun blasts: extra pellets; overload reduces damage taken.";
            case JAMUEL_BACUS -> "While primed: focus shot deals more damage (later in window = stronger).";
        };
    }

    public double getSkillCooldown() {
        return skillCooldown;
    }

    public String getSpriteAssetId() {
        return spriteAssetId;
    }

    /** Asset id for the signature skill PNG (surname filenames under {@code /assets/ui/skills/}). */
    public String getSkillIconAssetId() {
        return switch (this) {
            case JOSEPH_JIMENEZ -> "ui.skill.jimenez";
            case IBEN_ANOOS -> "ui.skill.anoos";
            case ILDE_JAN_FIGUERAS -> "ui.skill.figueras";
            case GAILE_AMOLONG -> "ui.skill.amolong";
            case JAMUEL_BACUS -> "ui.skill.bacus";
        };
    }

    /**
     * Pixel-accurate layout for each skill strip (content bbox + uniform cell grid), aligned to roster:
     * Joseph Jimenez, Iben Anoos, Ilde Jan Figueras, Gaille Amolong, Jamuel Bacus.
     * Ilde's sheet has no clean tile divisor across its width; it is drawn as one scaled banner.
     */
    public SkillIconSpec getSkillIconSpec() {
        return switch (this) {
            case JOSEPH_JIMENEZ -> new SkillIconSpec(5, 12, 3993, 121, 121, 121, 33, 9.0);
            case IBEN_ANOOS -> new SkillIconSpec(16, 41, 9238, 94, 149, 94, 62, 10.0);
            case ILDE_JAN_FIGUERAS -> new SkillIconSpec(38, 41, 6703, 88, 6703, 88, 1, 0.0);
            case GAILE_AMOLONG -> new SkillIconSpec(21, 56, 4375, 72, 125, 72, 35, 9.0);
            case JAMUEL_BACUS -> new SkillIconSpec(14, 83, 4687, 47, 43, 47, 109, 14.0);
        };
    }

    public boolean isFemaleVoice() {
        return femaleVoice;
    }

    public WeaponType getAssignedWeaponType() {
        return assignedWeaponType;
    }

    public String getAssignedWeaponLabel() {
        return switch (assignedWeaponType) {
            case ASSAULT_RIFLE -> "Assault Rifle";
            case SMG -> "SMG";
            case SHOTGUN -> "Shotgun";
            case SNIPER -> "Sniper";
            case LMG -> "LMG";
        };
    }

    public Weapon createWeapon() {
        return WeaponCatalog.create(assignedWeaponType);
    }

    /** Data-driven combat tuning (abilities, DoT, scaling hooks). */
    public CharacterCombatProfile getCombatProfile() {
        return CharacterCombatProfiles.forCharacter(this);
    }
}
