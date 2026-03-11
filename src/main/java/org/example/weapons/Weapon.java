package org.example.weapons;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * ABSTRACTION + ENCAPSULATION
 *
 * Abstract base class for all weapons.
 * Defines the contract every weapon must fulfill,
 * while hiding internal stat logic from the outside.
 *
 * Stat effects on the player:
 *   - moveSpeedModifier : multiplied against base speed
 *   - jumpForceModifier  : multiplied against base jump
 */
public abstract class Weapon {

    // ── Encapsulated fields ───────────────────────────────────
    private final String name;
    private final String description;
    private final int    damage;
    private final double fireRate;        // seconds between shots
    private final double projectileSpeed; // px/sec
    private final int    magazineSize;
    private final double reloadTime;      // seconds
    private final double moveSpeedModifier;
    private final double jumpForceModifier;
    private final int    pelletsPerShot;  // for shotgun spread; 1 for others
    private final double spread;          // angle spread in radians; 0 for precise

    protected Weapon(WeaponBuilder<?> builder) {
        this.name               = builder.name;
        this.description        = builder.description;
        this.damage             = builder.damage;
        this.fireRate           = builder.fireRate;
        this.projectileSpeed    = builder.projectileSpeed;
        this.magazineSize       = builder.magazineSize;
        this.reloadTime         = builder.reloadTime;
        this.moveSpeedModifier  = builder.moveSpeedModifier;
        this.jumpForceModifier  = builder.jumpForceModifier;
        this.pelletsPerShot     = builder.pelletsPerShot;
        this.spread             = builder.spread;
    }

    // ── Abstract methods (ABSTRACTION) ────────────────────────

    /** Returns the color used to draw this weapon's projectile. */
    public abstract Color getProjectileColor();

    /** Returns the weapon type label shown in UI. */
    public abstract WeaponType getType();

    /**
     * Draws a placeholder icon of this weapon onto the given GraphicsContext.
     * Replace with sprite rendering once assets are ready.
     */
    public abstract void drawIcon(GraphicsContext gc, double x, double y,
                                  double width, double height);

    // ── Getters (Encapsulation — read-only access) ────────────

    public String getName()              { return name; }
    public String getDescription()       { return description; }
    public int    getDamage()            { return damage; }
    public double getFireRate()          { return fireRate; }
    public double getProjectileSpeed()   { return projectileSpeed; }
    public int    getMagazineSize()      { return magazineSize; }
    public double getReloadTime()        { return reloadTime; }
    public double getMoveSpeedModifier() { return moveSpeedModifier; }
    public double getJumpForceModifier() { return jumpForceModifier; }
    public int    getPelletsPerShot()    { return pelletsPerShot; }
    public double getSpread()            { return spread; }

    // ── Stat summary string (for UI display) ──────────────────
    public String getStatSummary() {
        return String.format(
                "DMG: %d  |  RPM: %.0f  |  MAG: %d  |  RELOAD: %.1fs",
                damage,
                60.0 / fireRate,
                magazineSize,
                reloadTime
        );
    }

    // ── Builder (Encapsulation of construction logic) ─────────

    @SuppressWarnings("unchecked")
    public static abstract class WeaponBuilder<T extends WeaponBuilder<T>> {
        private String name          = "Unknown";
        private String description   = "";
        private int    damage        = 10;
        private double fireRate      = 0.5;
        private double projectileSpeed = 600;
        private int    magazineSize  = 30;
        private double reloadTime    = 2.0;
        private double moveSpeedModifier = 1.0;
        private double jumpForceModifier = 1.0;
        private int    pelletsPerShot = 1;
        private double spread        = 0.0;

        public T name(String v)               { name = v;               return (T) this; }
        public T description(String v)        { description = v;        return (T) this; }
        public T damage(int v)                { damage = v;             return (T) this; }
        public T fireRate(double v)           { fireRate = v;           return (T) this; }
        public T projectileSpeed(double v)    { projectileSpeed = v;    return (T) this; }
        public T magazineSize(int v)          { magazineSize = v;       return (T) this; }
        public T reloadTime(double v)         { reloadTime = v;         return (T) this; }
        public T moveSpeedModifier(double v)  { moveSpeedModifier = v;  return (T) this; }
        public T jumpForceModifier(double v)  { jumpForceModifier = v;  return (T) this; }
        public T pelletsPerShot(int v)        { pelletsPerShot = v;     return (T) this; }
        public T spread(double v)             { spread = v;             return (T) this; }

        public abstract Weapon build();
    }
}