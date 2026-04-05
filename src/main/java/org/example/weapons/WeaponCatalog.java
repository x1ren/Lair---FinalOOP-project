package org.example.weapons;

import javafx.scene.paint.Color;

public final class WeaponCatalog {

    private WeaponCatalog() {
    }

    public static Weapon create(WeaponType type) {
        return switch (type) {
            case ASSAULT_RIFLE -> new Weapon(type, "Assault Rifle", 0.12, 750, 30, 2.2, 1, 0.04, Color.DEEPSKYBLUE);
            case SMG -> new Weapon(type, "SMG", 0.06, 680, 40, 1.8, 1, 0.08, Color.LIMEGREEN);
            case SHOTGUN -> new Weapon(type, "Shotgun", 0.75, 600, 8, 3.0, 6, 0.30, Color.ORANGE);
            case SNIPER -> new Weapon(type, "Sniper", 1.8, 1400, 5, 3.5, 1, 0.0, Color.WHITE);
            case LMG -> new Weapon(type, "LMG", 0.09, 700, 100, 4.5, 1, 0.06, Color.TOMATO);
        };
    }
}
