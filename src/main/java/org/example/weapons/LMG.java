package org.example.weapons;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * INHERITANCE — extends Weapon
 *
 * LMG: massive magazine, sustained fire, heavy suppression.
 * Good damage, decent rate of fire — but the heaviest weapon.
 * Significant move and jump penalty. Brawler playstyle.
 *
 * Player stat modifiers:
 *   Move speed : -20%  (heaviest weapon)
 *   Jump force : -10%
 */
public class LMG extends Weapon {

    private LMG(Builder b) { super(b); }

    public static LMG create() {
        return new Builder()
                .name("LMG")
                .description("Suppression is the name of the game.\nMassive magazine — never stop firing. Slow but unstoppable.")
                .damage(20)
                .fireRate(0.09)          // ~667 RPM
                .projectileSpeed(700)
                .magazineSize(100)
                .reloadTime(4.5)         // long reload
                .moveSpeedModifier(0.80) // -20% speed
                .jumpForceModifier(0.90) // -10% jump
                .spread(0.06)
                .build();
    }

    @Override public Color getProjectileColor() { return Color.TOMATO; }
    @Override public WeaponType getType()        { return WeaponType.LMG; }

    @Override
    public void drawIcon(GraphicsContext gc, double x, double y,
                         double w, double h) {
        gc.setFill(Color.DARKSLATEGRAY);
        // Chunky stock
        gc.fillRect(x, y + h * 0.40, w * 0.22, h * 0.22);
        // Heavy receiver
        gc.fillRect(x + w * 0.18, y + h * 0.28, w * 0.50, h * 0.42);
        // Long barrel with shroud
        gc.fillRect(x + w * 0.65, y + h * 0.38, w * 0.35, h * 0.16);
        // Drum magazine
        gc.setFill(Color.GRAY);
        gc.fillOval(x + w * 0.28, y + h * 0.55, w * 0.28, h * 0.35);
        // Carry handle
        gc.setFill(Color.DIMGRAY);
        gc.fillRect(x + w * 0.30, y + h * 0.18, w * 0.28, h * 0.08);
        gc.fillRect(x + w * 0.30, y + h * 0.18, w * 0.04, h * 0.14);
        gc.fillRect(x + w * 0.54, y + h * 0.18, w * 0.04, h * 0.14);
        // Bipod
        gc.setFill(Color.SLATEGRAY);
        gc.fillRect(x + w * 0.68, y + h * 0.54, w * 0.04, h * 0.22);
        gc.fillRect(x + w * 0.77, y + h * 0.54, w * 0.04, h * 0.22);
        // Muzzle flash tint
        gc.setFill(Color.color(1.0, 0.3, 0.1, 0.6));
        gc.fillOval(x + w * 0.94, y + h * 0.35, w * 0.08, h * 0.22);
    }

    public static class Builder extends WeaponBuilder<Builder> {
        @Override public LMG build() { return new LMG(this); }
    }
}