package org.example.weapons;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * INHERITANCE — extends Weapon
 *
 * SMG: fastest fire rate, low damage per bullet.
 * Great mobility — lightest weapon, speed boost.
 * Rewards aggressive, close-range play.
 *
 * Player stat modifiers:
 *   Move speed : +10%  (lightest weapon)
 *   Jump force : +5%   (nimble)
 */
public class SMG extends Weapon {

    private SMG(Builder b) { super(b); }

    public static SMG create() {
        return new Builder()
                .name("SMG")
                .description("Light and lethal up close.\nThe fastest fire rate of any weapon — overwhelm them.")
                .damage(12)
                .fireRate(0.06)          // ~1000 RPM
                .projectileSpeed(680)
                .magazineSize(40)
                .reloadTime(1.8)
                .moveSpeedModifier(1.10) // +10% speed
                .jumpForceModifier(1.05) // +5% jump
                .spread(0.08)            // more spray
                .build();
    }

    @Override public Color getProjectileColor() { return Color.LIMEGREEN; }
    @Override public WeaponType getType()        { return WeaponType.SMG; }

    @Override
    public void drawIcon(GraphicsContext gc, double x, double y,
                         double w, double h) {
        gc.setFill(Color.DARKSLATEGRAY);
        // Compact body
        gc.fillRect(x + w * 0.15, y + h * 0.30, w * 0.55, h * 0.35);
        // Short barrel
        gc.fillRect(x + w * 0.68, y + h * 0.38, w * 0.22, h * 0.12);
        // Folded stock nub
        gc.fillRect(x, y + h * 0.40, w * 0.18, h * 0.12);
        // Mag (angled forward)
        gc.setFill(Color.GRAY);
        gc.fillRect(x + w * 0.35, y + h * 0.62, w * 0.12, h * 0.25);
        // Grip
        gc.fillRect(x + w * 0.20, y + h * 0.62, w * 0.10, h * 0.20);
        // Sight dot
        gc.setFill(Color.LIMEGREEN);
        gc.fillOval(x + w * 0.50, y + h * 0.24, w * 0.08, h * 0.08);
    }

    public static class Builder extends WeaponBuilder<Builder> {
        @Override public SMG build() { return new SMG(this); }
    }
}