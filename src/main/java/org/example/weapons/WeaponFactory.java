package org.example.weapons;

import java.util.List;

/**
 * Factory that provides all available weapon instances.
 * Centralizes weapon creation — add new weapons here.
 */
public class WeaponFactory {

    public static List<Weapon> getAllWeapons() {
        return List.of(
                AssaultRifle.create(),
                SMG.create(),
                Shotgun.create(),
                Sniper.create(),
                LMG.create()
        );
    }

    public static Weapon getDefault() {
        return AssaultRifle.create();
    }
}