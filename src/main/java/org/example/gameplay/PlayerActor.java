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
        double x = Math.round(getX());
        double y = Math.round(getY());
        double pixel = 4;

        gc.setFill(Color.color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 0.22));
        gc.fillRect(x - 8, y - 8, getWidth() + 16, getHeight() + 16);

        gc.setFill(Color.color(0.80, 0.86, 0.90));
        gc.fillRect(x + pixel * 2, y, pixel * 6, pixel * 3);
        gc.fillRect(x + pixel, y + pixel * 3, pixel * 8, pixel * 7);

        gc.setFill(Color.color(0.18, 0.20, 0.24));
        gc.fillRect(x + pixel * 2, y + pixel * 4, pixel * 2, pixel * 2);
        gc.fillRect(x + pixel * 6, y + pixel * 4, pixel * 2, pixel * 2);

        gc.setFill(Color.color(0.64, 0.70, 0.76));
        gc.fillRect(x + pixel * 2, y + pixel * 10, pixel * 2, pixel * 4);
        gc.fillRect(x + pixel * 6, y + pixel * 10, pixel * 2, pixel * 4);

        gc.setFill(Color.color(auraColor.getRed(), auraColor.getGreen(), auraColor.getBlue(), 0.95));
        gc.fillRect(x + (facing > 0 ? pixel * 8 : -pixel * 2), y + pixel * 5, pixel * 4, pixel);
        gc.fillRect(x + (facing > 0 ? pixel * 10 : -pixel * 4), y + pixel * 4, pixel * 2, pixel * 3);
    }
}
