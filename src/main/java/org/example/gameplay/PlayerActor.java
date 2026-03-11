package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PlayerActor extends GameObject {

    private double vx;
    private double vy;
    private int facing = 1;
    private boolean onGround;
    private Color auraColor = Color.DEEPSKYBLUE;

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
        gc.setFill(Color.color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 0.18));
        gc.fillOval(getX() - 18, getY() - 18, getWidth() + 36, getHeight() + 36);

        gc.setFill(Color.color(0.9, 0.94, 0.96));
        gc.fillRoundRect(getX(), getY(), getWidth(), getHeight(), 8, 8);

        gc.setFill(Color.color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 0.9));
        gc.fillRect(getX() + (facing > 0 ? getWidth() : -10), getY() + 18, 14, 4);
    }
}
