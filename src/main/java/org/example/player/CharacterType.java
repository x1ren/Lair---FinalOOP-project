package org.example.player;

import org.example.weapons.Weapon;
import org.example.weapons.WeaponFactory;
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
            6.0
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
            6.0
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
            5.0
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
            6.0
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
            6.0
    );

    public static final double BASE_MOVE_SPEED = 260.0;
    public static final double BASE_JUMP = -520.0;

    public final String name;
    public final String title;
    public final String lore;

    private final WeaponType assignedWeaponType;
    private final int health;
    private final int damage;
    private final int movementSpeed;
    private final String skillName;
    private final double skillCooldown;

    CharacterType(String name, String title, String lore,
                  WeaponType assignedWeaponType,
                  int health, int damage, int movementSpeed,
                  String skillName, double skillCooldown) {
        this.name = name;
        this.title = title;
        this.lore = lore;
        this.assignedWeaponType = assignedWeaponType;
        this.health = health;
        this.damage = damage;
        this.movementSpeed = movementSpeed;
        this.skillName = skillName;
        this.skillCooldown = skillCooldown;
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

    public double getSkillCooldown() {
        return skillCooldown;
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
        return WeaponFactory.create(assignedWeaponType);
    }
}
