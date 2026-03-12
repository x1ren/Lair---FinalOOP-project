package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class EnemyActor extends GameObject {

    private final String name;
    private int hp;
    private final int maxHp;
    private final double speed;
    private final Color color;
    private final boolean boss;
    private double attackCooldown;
    private double slowTimer;
    private int bleedTicks;
    private int bleedTickDamage;
    private double bleedTickTimer;

    public EnemyActor(String name, double x, double y, double width, double height,
                      int hp, int maxHp, double speed, Color color, boolean boss) {
        super(x, y, width, height);
        this.name = name;
        this.hp = hp;
        this.maxHp = maxHp;
        this.speed = speed;
        this.color = color;
        this.boss = boss;
    }

    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public double getSpeed() {
        return speed;
    }

    public Color getColor() {
        return color;
    }

    public boolean isBoss() {
        return boss;
    }

    public double getAttackCooldown() {
        return attackCooldown;
    }

    public void setAttackCooldown(double attackCooldown) {
        this.attackCooldown = attackCooldown;
    }

    public void takeDamage(int damage) {
        hp -= damage;
    }

    public void applySlow(double duration) {
        slowTimer = Math.max(slowTimer, duration);
    }

    public void applyBleed(int totalDamage) {
        bleedTicks += 3;
        bleedTickDamage = Math.max(bleedTickDamage, Math.max(1, totalDamage / 3));
        if (bleedTickTimer <= 0) {
            bleedTickTimer = 0.35;
        }
    }

    public boolean isDefeated() {
        return hp <= 0;
    }

    public void updateStatusEffects(double dt) {
        slowTimer = Math.max(0, slowTimer - dt);

        if (bleedTicks <= 0) {
            return;
        }

        bleedTickTimer -= dt;
        if (bleedTickTimer <= 0) {
            takeDamage(bleedTickDamage);
            bleedTicks--;
            bleedTickTimer = 0.35;
            if (bleedTicks <= 0) {
                bleedTickDamage = 0;
            }
        }
    }

    public void chase(PlayerActor player, double dt, double minX, double maxX) {
        double direction = Math.signum(player.getCenterX() - getCenterX());
        double effectiveSpeed = slowTimer > 0 ? speed * 0.55 : speed;
        moveBy(direction * effectiveSpeed * dt, 0);
        setX(Math.max(minX, Math.min(maxX, getX())));
    }

    @Override
    public void render(GraphicsContext gc) {
        double x = Math.round(getX());
        double y = Math.round(getY());
        double pixel = boss ? 6 : 4;

        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.18));
        gc.fillRect(x - pixel * 2, y - pixel * 2, getWidth() + pixel * 4, getHeight() + pixel * 4);

        gc.setFill(color);
        gc.fillRect(x + pixel, y, getWidth() - pixel * 2, pixel * 3);
        gc.fillRect(x, y + pixel * 3, getWidth(), getHeight() - pixel * 3);

        gc.setFill(Color.color(0.08, 0.08, 0.10));
        gc.fillRect(x + pixel * 2, y + pixel * 4, pixel * 2, pixel * 2);
        gc.fillRect(x + getWidth() - pixel * 4, y + pixel * 4, pixel * 2, pixel * 2);

        gc.setFill(Color.color(0.1, 0.1, 0.12, 0.95));
        gc.fillRect(x, y - pixel * 2, getWidth(), pixel);
        gc.setFill(Color.color(0.9, 0.16, 0.16, 0.95));
        gc.fillRect(x, y - pixel * 2, getWidth() * (hp / (double) maxHp), pixel);

        if (slowTimer > 0) {
            gc.setFill(Color.color(0.25, 0.7, 1.0, 0.3));
            gc.fillRect(x - pixel, y - pixel, getWidth() + pixel * 2, getHeight() + pixel * 2);
        }

        if (bleedTicks > 0) {
            gc.setFill(Color.color(1.0, 0.12, 0.18, 0.24));
            gc.fillRect(x - pixel * 2, y - pixel * 2, getWidth() + pixel * 4, getHeight() + pixel * 4);
        }

        if (boss) {
            gc.setFont(javafx.scene.text.Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 12));
            gc.setFill(Color.WHITE);
            gc.fillText(name, x - 10, y - 16);
        }
    }
}
