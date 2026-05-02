package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SplittingSpike extends GameObject {
    
    private final double vx;
    private final double vy;
    private final int damage;
    private double lifetime;
    private boolean hasSplit;
    
    public SplittingSpike(double x, double y, double vx, double vy, int damage) {
        super(x, y, 32, 32);
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.lifetime = 3.0;
        this.hasSplit = false;
    }
    
    public void update(double dt) {
        moveBy(vx * dt, vy * dt);
        lifetime -= dt;
    }
    
    public boolean isExpired() {
        return lifetime <= 0;
    }
    
    public boolean shouldSplit() {
        return !hasSplit && lifetime < 2.0;
    }
    
    public void markSplit() {
        hasSplit = true;
    }
    
    public int getDamage() {
        return damage;
    }
    
    @Override
    public void render(GraphicsContext gc) {
        double x = Math.round(getX());
        double y = Math.round(getY());
        
        // Draw spinning spike projectile
        gc.setFill(Color.color(0.3, 0.15, 0.25, 0.4));
        gc.fillOval(x - 4, y - 4, getWidth() + 8, getHeight() + 8);
        
        gc.setFill(Color.color(0.5, 0.25, 0.4, 0.9));
        gc.fillOval(x, y, getWidth(), getHeight());
        
        gc.setFill(Color.color(0.7, 0.4, 0.6, 0.8));
        gc.fillOval(x + 8, y + 8, getWidth() - 16, getHeight() - 16);
    }
}
