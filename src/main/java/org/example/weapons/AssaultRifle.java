package org.example.weapons;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * INHERITANCE — extends Weapon
 *
 * Assault Rifle: balanced all-rounder.
 * Good damage, decent fire rate, medium mag.
 * Slight speed penalty from carrying it.
 *
 * Player stat modifiers:
 *   Move speed : -5%  (slight weight)
 *   Jump force : no change
 */
public class AssaultRifle extends Weapon {

    private AssaultRifle(Builder b) { super(b); }

    public static AssaultRifle create() {
        return new Builder()
                .name("Assault Rifle")
                .description("Balanced and reliable. A soldier's best friend.\nSteady fire rate with solid damage output.")
                .damage(25)
                .fireRate(0.12)          // ~500 RPM
                .projectileSpeed(750)
                .magazineSize(30)
                .reloadTime(2.2)
                .moveSpeedModifier(0.95) // -5% speed
                .jumpForceModifier(1.0)
                .spread(0.04)            // slight spread
                .build();
    }

    @Override public Color getProjectileColor() { return Color.DEEPSKYBLUE; }
    @Override public WeaponType getType()        { return WeaponType.ASSAULT_RIFLE; }

    @Override
    public void drawIcon(GraphicsContext gc, double x, double y,
                         double w, double h) {
        // Placeholder geometric icon — replace with sprite later
        gc.setFill(Color.SLATEGRAY);

        // Stock
        gc.fillRect(x, y + h * 0.45, w * 0.25, h * 0.15);
        // Body
        gc.fillRect(x + w * 0.2, y + h * 0.35, w * 0.55, h * 0.30);
        // Barrel
        gc.fillRect(x + w * 0.72, y + h * 0.42, w * 0.28, h * 0.13);
        // Magazine
        gc.setFill(Color.DIMGRAY);
        gc.fillRect(x + w * 0.38, y + h * 0.62, w * 0.15, h * 0.28);
        // Grip
        gc.fillRect(x + w * 0.22, y + h * 0.60, w * 0.12, h * 0.22);
        // Sight
        gc.setFill(Color.DEEPSKYBLUE);
        gc.fillRect(x + w * 0.45, y + h * 0.28, w * 0.20, h * 0.08);
    }

    // ── Builder ───────────────────────────────────────────────
    public static class Builder extends WeaponBuilder<Builder> {
        @Override public AssaultRifle build() { return new AssaultRifle(this); }
    }
}