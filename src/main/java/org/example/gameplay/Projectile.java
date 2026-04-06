package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Projectile extends GameObject {

    private final double vx;
    private final double vy;
    private final HitPayload hitPayload;
    private final BleedConfig bleedConfig;
    private final SlowConfig slowConfig;
    private final Color color;
    private final double radius;
    private double life;

    public Projectile(double x, double y, double vx, double vy, int damage, Color color) {
        this(x, y, vx, vy, HitPayload.ofDamage(damage), color, BleedConfig.legacyDefaults(), SlowConfig.none());
    }

    public Projectile(double x, double y, double vx, double vy, int damage, Color color,
                      int bleedDamage, double slowDuration) {
        this(x, y, vx, vy,
                HitPayload.withBleedSlow(damage, bleedDamage, slowDuration),
                color,
                BleedConfig.legacyDefaults(),
                SlowConfig.none());
    }

    public Projectile(double x, double y, double vx, double vy,
                      HitPayload hitPayload, Color color,
                      BleedConfig bleedConfig, SlowConfig slowConfig) {
        super(x - 4, y - 4, 8, 8);
        this.vx = vx;
        this.vy = vy;
        this.hitPayload = hitPayload;
        this.color = color;
        this.radius = 4;
        this.bleedConfig = bleedConfig == null ? BleedConfig.legacyDefaults() : bleedConfig;
        this.slowConfig = slowConfig == null ? SlowConfig.none() : slowConfig;
        this.life = 1.7;
    }

    public HitPayload getHitPayload() {
        return hitPayload;
    }

    public BleedConfig getBleedConfig() {
        return bleedConfig;
    }

    public SlowConfig getSlowConfig() {
        return slowConfig;
    }

    public int getDamage() {
        return hitPayload.getDirectDamage();
    }

    public double getRadius() {
        return radius;
    }

    public int getBleedDamage() {
        return hitPayload.bleedTotalOrZero();
    }

    public double getSlowDuration() {
        return hitPayload.slowDurationOrZero();
    }

    public double getDrawX() {
        return getX() + radius;
    }

    public double getDrawY() {
        return getY() + radius;
    }

    public void update(double dt) {
        moveBy(vx * dt, vy * dt);
        life -= dt;
    }

    public boolean isExpired(double worldWidth, double worldHeight) {
        return life <= 0
                || getX() < -40 || getX() > worldWidth + 40
                || getY() < -40 || getY() > worldHeight + 40;
    }

    @Override
    public void render(GraphicsContext gc) {
        double x = Math.round(getX());
        double y = Math.round(getY());
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.35));
        gc.fillRect(x - 2, y - 2, getWidth() + 4, getHeight() + 4);
        gc.setFill(color);
        gc.fillRect(x, y, getWidth(), getHeight());
    }
}
