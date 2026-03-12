package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Projectile extends GameObject {

    private final double vx;
    private final double vy;
    private final int damage;
    private final Color color;
    private final double radius;
    private double life;

    public Projectile(double x, double y, double vx, double vy, int damage, Color color) {
        super(x - 4, y - 4, 8, 8);
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.color = color;
        this.radius = 4;
        this.life = 1.7;
    }

    public int getDamage() {
        return damage;
    }

    public double getRadius() {
        return radius;
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
