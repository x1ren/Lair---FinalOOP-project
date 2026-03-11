package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;

/**
 * Abstract base for objects that exist in the gameplay world.
 */
public abstract class GameObject {

    private double x;
    private double y;
    private double width;
    private double height;

    protected GameObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getCenterX() {
        return x + width / 2.0;
    }

    public double getCenterY() {
        return y + height / 2.0;
    }

    public void moveBy(double dx, double dy) {
        x += dx;
        y += dy;
    }

    public abstract void render(GraphicsContext gc);
}
