package org.example.weapons;

import java.util.List;

/**
 * Factory that provides all available weapon instances.
 * Centralizes weapon creation — add new weapons here.
 */
public class WeaponFactory {

    public static Weapon create(WeaponType type) {
        return switch (type) {
            case ASSAULT_RIFLE -> AssaultRifle.create();
            case SMG -> SMG.create();
            case SHOTGUN -> Shotgun.create();
            case SNIPER -> Sniper.create();
            case LMG -> LMG.create();
        };
    }

    public static List<Weapon> getAllWeapons() {
        return List.of(
                create(WeaponType.ASSAULT_RIFLE),
                create(WeaponType.SMG),
                create(WeaponType.SHOTGUN),
                create(WeaponType.SNIPER),
                create(WeaponType.LMG)
        );
    }

    public static Weapon getDefault() {
        return create(WeaponType.ASSAULT_RIFLE);
    }
}
