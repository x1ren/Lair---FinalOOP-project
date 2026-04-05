package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.assets.AnimationStrip;
import org.example.assets.SpriteSheet;

public class EnemyActor extends GameObject {

    private final String name;
    private int hp;
    private final int maxHp;
    private final double speed;
    private final Color color;
    private final boolean boss;
    private double attackCooldown;
    private double vy;
    private boolean onGround;
    private double jumpCooldown;
    private double slowTimer;
    private int bleedTicks;
    private int bleedTickDamage;
    private double bleedTickTimer;
    private SpriteSheet spriteSheet;
    private AnimationStrip idleStrip;
    private AnimationStrip walkStrip;
    private AnimationStrip attackStrip;
    private double animationTime;
    private boolean moving;
    private boolean attacking;
    private int facing = -1;

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

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void takeDamage(int damage) {
        hp -= damage;
    }

    public void setSpriteSheet(SpriteSheet spriteSheet, AnimationStrip idleStrip,
                               AnimationStrip walkStrip, AnimationStrip attackStrip) {
        this.spriteSheet = spriteSheet;
        this.idleStrip = idleStrip;
        this.walkStrip = walkStrip;
        this.attackStrip = attackStrip;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public void stepVertical(double dt) {
        moveBy(0, vy * dt);
    }

    public void landOn(double surfaceY) {
        setY(surfaceY - getHeight());
        vy = 0;
        onGround = true;
    }

    public boolean canJump() {
        return onGround && jumpCooldown <= 0;
    }

    public void jump(double jumpVelocity) {
        vy = jumpVelocity;
        onGround = false;
        jumpCooldown = 0.9;
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
        animationTime += dt;
        slowTimer = Math.max(0, slowTimer - dt);
        jumpCooldown = Math.max(0, jumpCooldown - dt);

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
        moving = Math.abs(direction) > 0;
        if (direction != 0) {
            facing = direction < 0 ? -1 : 1;
        }
        moveBy(direction * effectiveSpeed * dt, 0);
        setX(Math.max(minX, Math.min(maxX, getX())));
    }

    @Override
    public void render(GraphicsContext gc) {
        double x = Math.round(getX());
        double y = Math.round(getY());
        double pixel = boss ? 6 : 4;

        if (spriteSheet != null) {
            AnimationStrip strip = resolveStrip();
            int row = strip == null ? 0 : strip.row();
            int column = strip == null ? 0 : strip.frameAt(animationTime);
            spriteSheet.drawFrame(gc, row, column, x - (boss ? 6 : 4), y - (boss ? 10 : 6), getWidth() + (boss ? 12 : 8), getHeight() + (boss ? 12 : 8), facing > 0);
        } else {
            gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.18));
            gc.fillRect(x - pixel * 2, y - pixel * 2, getWidth() + pixel * 4, getHeight() + pixel * 4);

            gc.setFill(color);
            gc.fillRect(x + pixel, y, getWidth() - pixel * 2, pixel * 3);
            gc.fillRect(x, y + pixel * 3, getWidth(), getHeight() - pixel * 3);

            gc.setFill(Color.color(0.08, 0.08, 0.10));
            gc.fillRect(x + pixel * 2, y + pixel * 4, pixel * 2, pixel * 2);
            gc.fillRect(x + getWidth() - pixel * 4, y + pixel * 4, pixel * 2, pixel * 2);
        }

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

    private AnimationStrip resolveStrip() {
        if (attacking && attackStrip != null) {
            return attackStrip;
        }
        if (moving && walkStrip != null) {
            return walkStrip;
        }
        if (idleStrip != null) {
            return idleStrip;
        }
        return walkStrip != null ? walkStrip : attackStrip;
    }
}
