package org.example.weapons;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * INHERITANCE — extends Weapon
 *
 * Shotgun: fires 6 pellets per shot in a spread cone.
 * Devastating at close range, useless at long range.
 * Heavy — moderate speed penalty.
 *
 * Player stat modifiers:
 *   Move speed : -8%
 *   Jump force : -3%
 */
public class Shotgun extends Weapon {

    private Shotgun(Builder b) { super(b); }

    public static Shotgun create() {
        return new Builder()
                .name("Shotgun")
                .description("One shot, one room cleared.\nFires 6 pellets. Devastating up close — terrible at range.")
                .damage(18)              // per pellet — up to 108 total
                .fireRate(0.75)          // ~80 RPM
                .projectileSpeed(600)
                .magazineSize(8)
                .reloadTime(3.0)
                .moveSpeedModifier(0.92) // -8% speed
                .jumpForceModifier(0.97) // -3% jump
                .pelletsPerShot(6)       // 6 pellets per shot
                .spread(0.30)            // wide cone
                .build();
    }

    @Override public Color getProjectileColor() { return Color.ORANGE; }
    @Override public WeaponType getType()        { return WeaponType.SHOTGUN; }

    @Override
    public void drawIcon(GraphicsContext gc, double x, double y,
                         double w, double h) {
        gc.setFill(Color.SADDLEBROWN);
        // Long wooden stock
        gc.fillRect(x, y + h * 0.42, w * 0.35, h * 0.18);
        // Action/receiver
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(x + w * 0.30, y + h * 0.35, w * 0.30, h * 0.32);
        // Long barrel
        gc.fillRect(x + w * 0.58, y + h * 0.40, w * 0.42, h * 0.14);
        // Pump grip
        gc.setFill(Color.SADDLEBROWN);
        gc.fillRect(x + w * 0.60, y + h * 0.55, w * 0.18, h * 0.18);
        // Muzzle brake
        gc.setFill(Color.ORANGE);
        gc.fillRect(x + w * 0.95, y + h * 0.36, w * 0.05, h * 0.22);
    }

    public static class Builder extends WeaponBuilder<Builder> {
        @Override public Shotgun build() { return new Shotgun(this); }
    }
}