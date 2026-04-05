package org.example.weapons;

import javafx.scene.paint.Color;

public final class Weapon {

    private final WeaponType type;
    private final String name;
    private final double fireRate;
    private final double projectileSpeed;
    private final int magazineSize;
    private final double reloadTime;
    private final int pelletsPerShot;
    private final double spread;
    private final Color projectileColor;

    public Weapon(WeaponType type, String name, double fireRate, double projectileSpeed,
                  int magazineSize, double reloadTime, int pelletsPerShot,
                  double spread, Color projectileColor) {
        this.type = type;
        this.name = name;
        this.fireRate = fireRate;
        this.projectileSpeed = projectileSpeed;
        this.magazineSize = magazineSize;
        this.reloadTime = reloadTime;
        this.pelletsPerShot = pelletsPerShot;
        this.spread = spread;
        this.projectileColor = projectileColor;
    }

    public WeaponType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getFireRate() {
        return fireRate;
    }

    public double getProjectileSpeed() {
        return projectileSpeed;
    }

    public int getMagazineSize() {
        return magazineSize;
    }

    public double getReloadTime() {
        return reloadTime;
    }

    public int getPelletsPerShot() {
        return pelletsPerShot;
    }

    public double getSpread() {
        return spread;
    }

    public Color getProjectileColor() {
        return projectileColor;
    }
}
