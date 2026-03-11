package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class EnemyActor extends GameObject {

    private final String name;
    private int hp;
    private final int maxHp;
    private final double speed;
    private final Color color;
    private final boolean boss;
    private double attackCooldown;

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

    public boolean isDefeated() {
        return hp <= 0;
    }

    public void chase(PlayerActor player, double dt, double minX, double maxX) {
        double direction = Math.signum(player.getCenterX() - getCenterX());
        moveBy(direction * speed * dt, 0);
        setX(Math.max(minX, Math.min(maxX, getX())));
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.18));
        gc.fillOval(getX() - 14, getY() - 12, getWidth() + 28, getHeight() + 24);

        gc.setFill(color);
        gc.fillRoundRect(getX(), getY(), getWidth(), getHeight(), 8, 8);

        gc.setFill(Color.color(0.1, 0.1, 0.12, 0.85));
        gc.fillRect(getX(), getY() - 12, getWidth(), 6);
        gc.setFill(Color.color(0.9, 0.16, 0.16, 0.95));
        gc.fillRect(getX(), getY() - 12, getWidth() * (hp / (double) maxHp), 6);

        if (boss) {
            gc.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
            gc.setFill(Color.WHITE);
            gc.fillText(name, getX() - 10, getY() - 18);
        }
    }
}
