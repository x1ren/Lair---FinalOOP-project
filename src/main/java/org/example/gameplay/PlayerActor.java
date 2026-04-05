package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.assets.AnimationState;
import org.example.assets.SpriteSet;

public class PlayerActor extends GameObject {

    private double vx;
    private double vy;
    private int facing = 1;
    private boolean onGround;
    private Color auraColor = Color.DEEPSKYBLUE;
    private SpriteSet spriteSet;
    private double animationTime;
    private double hitTimer;
    private boolean defeated;

    public PlayerActor(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public int getFacing() {
        return facing;
    }

    public void setFacing(int facing) {
        this.facing = facing;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void setAuraColor(Color auraColor) {
        this.auraColor = auraColor;
    }

    public void setSpriteSet(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }

    public void updateAnimation(double dt) {
        animationTime += dt;
        hitTimer = Math.max(0, hitTimer - dt);
    }

    public void triggerHit() {
        hitTimer = 0.22;
    }

    public void markDefeated() {
        defeated = true;
    }

    public void step(double dt) {
        moveBy(vx * dt, vy * dt);
    }

    public void clampX(double minX, double maxX) {
        setX(Math.max(minX, Math.min(maxX, getX())));
    }

    public void landOn(double surfaceY) {
        setY(surfaceY - getHeight());
        vy = 0;
        onGround = true;
    }

    @Override
    public void render(GraphicsContext gc) {
        double x = Math.round(getX());
        double y = Math.round(getY());
        gc.setFill(Color.color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 0.22));
        gc.fillRect(x - 8, y - 8, getWidth() + 16, getHeight() + 16);

        if (spriteSet != null) {
            spriteSet.draw(gc, resolveAnimationState(), animationTime, x - 10, y - 6, 64, 64, facing < 0);
            return;
        }

        gc.setFill(Color.color(0.80, 0.86, 0.90));
        gc.fillRect(x + 8, y, getWidth() - 16, 12);
        gc.fillRect(x + 4, y + 12, getWidth() - 8, getHeight() - 12);
    }

    private AnimationState resolveAnimationState() {
        if (defeated) {
            return AnimationState.DEATH;
        }
        if (hitTimer > 0) {
            return AnimationState.HIT;
        }
        if (!onGround && vy < 0) {
            return AnimationState.JUMP;
        }
        if (!onGround) {
            return AnimationState.FALL;
        }
        if (Math.abs(vx) > 10) {
            return AnimationState.WALK;
        }
        return AnimationState.IDLE;
    }
}
