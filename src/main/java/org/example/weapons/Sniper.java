package org.example.weapons;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * INHERITANCE — extends Weapon
 *
 * Sniper: highest single-shot damage in the game.
 * Extremely slow fire rate, tiny mag, long reload.
 * Heavy — significant speed penalty. Rewards patience.
 *
 * Player stat modifiers:
 *   Move speed : -15%  (heavy + scope)
 *   Jump force : -5%
 */
public class Sniper extends Weapon {

    private Sniper(Builder b) { super(b); }

    public static Sniper create() {
        return new Builder()
                .name("Sniper")
                .description("One bullet. One kill.\nHighest damage per shot. Punishes movement — demands patience.")
                .damage(90)
                .fireRate(1.8)           // ~33 RPM — bolt action feel
                .projectileSpeed(1400)   // fastest projectile
                .magazineSize(5)
                .reloadTime(3.5)
                .moveSpeedModifier(0.85) // -15% speed
                .jumpForceModifier(0.95) // -5% jump
                .spread(0.0)             // perfect accuracy
                .build();
    }

    @Override public Color getProjectileColor() { return Color.WHITE; }
    @Override public WeaponType getType()        { return WeaponType.SNIPER; }

    @Override
    public void drawIcon(GraphicsContext gc, double x, double y,
                         double w, double h) {
        gc.setFill(Color.DARKGRAY);
        // Long stock
        gc.fillRect(x, y + h * 0.44, w * 0.28, h * 0.16);
        // Body
        gc.fillRect(x + w * 0.24, y + h * 0.36, w * 0.35, h * 0.28);
        // Very long barrel
        gc.fillRect(x + w * 0.56, y + h * 0.41, w * 0.44, h * 0.12);
        // Scope
        gc.setFill(Color.color(0.1, 0.1, 0.15));
        gc.fillRect(x + w * 0.35, y + h * 0.20, w * 0.28, h * 0.18);
        // Scope lens glow
        gc.setFill(Color.color(0.8, 0.95, 1.0, 0.8));
        gc.fillOval(x + w * 0.56, y + h * 0.22, w * 0.07, h * 0.14);
        // Bipod legs
        gc.setFill(Color.GRAY);
        gc.fillRect(x + w * 0.60, y + h * 0.62, w * 0.04, h * 0.20);
        gc.fillRect(x + w * 0.68, y + h * 0.62, w * 0.04, h * 0.20);
        // Muzzle
        gc.setFill(Color.WHITE);
        gc.fillRect(x + w * 0.97, y + h * 0.39, w * 0.03, h * 0.16);
    }

    public static class Builder extends WeaponBuilder<Builder> {
        @Override public Sniper build() { return new Sniper(this); }
    }
}