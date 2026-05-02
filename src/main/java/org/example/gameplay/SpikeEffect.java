package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SpikeEffect extends GameObject {
    
    private final String type;
    private double lifetime;
    private final double maxLifetime;
    private final int damage;
    private boolean hasHit;
    
    public SpikeEffect(String type, double x, double y, int damage) {
        super(x, y, 24, 48);
        this.type = type;
        this.damage = damage;
        this.maxLifetime = type.equals("ground") ? 1.2 : 1.5;
        this.lifetime = maxLifetime;
        this.hasHit = false;
    }
    
    public void update(double dt) {
        lifetime -= dt;
        
        // Ground spikes rise up faster
        if (type.equals("ground") && lifetime > maxLifetime * 0.7) {
            double progress = (maxLifetime - lifetime) / (maxLifetime * 0.3);
            setY(getY() - 250 * dt * progress);  // Increased from 150 to 250
        }
        
        // Rain spikes fall down faster
        if (type.equals("rain")) {
            moveBy(0, 650 * dt);  // Increased from 400 to 650
        }
    }
    
    public boolean isExpired() {
        return lifetime <= 0;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public boolean hasHit() {
        return hasHit;
    }
    
    public void markHit() {
        hasHit = true;
    }
    
    @Override
    public void render(GraphicsContext gc) {
        double x = Math.round(getX());
        double y = Math.round(getY());
        double alpha = Math.min(1.0, lifetime / maxLifetime);
        
        // Draw spike shape
        gc.setFill(Color.color(0.2, 0.1, 0.15, alpha * 0.3));
        gc.fillRect(x - 2, y - 2, getWidth() + 4, getHeight() + 4);
        
        gc.setFill(Color.color(0.4, 0.2, 0.3, alpha * 0.8));
        double[] xPoints = {x + getWidth() / 2, x, x + getWidth()};
        double[] yPoints = {y, y + getHeight(), y + getHeight()};
        gc.fillPolygon(xPoints, yPoints, 3);
        
        gc.setFill(Color.color(0.6, 0.3, 0.5, alpha));
        double[] xPoints2 = {x + getWidth() / 2, x + 4, x + getWidth() - 4};
        double[] yPoints2 = {y + 4, y + getHeight() - 4, y + getHeight() - 4};
        gc.fillPolygon(xPoints2, yPoints2, 3);
    }
}
