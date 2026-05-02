package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class BossSkill extends GameObject {
    
    private final String name;
    private final double vx;
    private final double vy;
    private final int damage;
    private final String effectType;
    private double lifetime;
    private final Color purpleColor;
    private final double radius;
    
    public BossSkill(String name, double x, double y, double vx, double vy, int damage, String effectType) {
        super(x - 10, y - 10, 20, 20);
        this.name = name;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.effectType = effectType;
        this.lifetime = 6.0;
        this.purpleColor = Color.color(0.6, 0.2, 0.9);
        this.radius = 10;
    }
    
    public void update(double dt) {
        moveBy(vx * dt, vy * dt);
        lifetime -= dt;
    }
    
    public boolean isExpired() {
        return lifetime <= 0;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public String getEffectType() {
        return effectType;
    }
    
    public String getName() {
        return name;
    }
    
    public double getRadius() {
        return radius;
    }
    
    public double getCenterX() {
        return getX() + radius;
    }
    
    public double getCenterY() {
        return getY() + radius;
    }
    
    @Override
    public void render(GraphicsContext gc) {
        double centerX = getCenterX();
        double centerY = getCenterY();
        
        // Render as big visible purple circle (bullet hell style)
        // Outer glow
        gc.setFill(Color.color(0.6, 0.2, 0.9, 0.3));
        gc.fillOval(centerX - radius - 4, centerY - radius - 4, (radius + 4) * 2, (radius + 4) * 2);
        
        // Main circle
        gc.setFill(purpleColor);
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        
        // Inner highlight
        gc.setFill(Color.color(0.8, 0.5, 1.0, 0.7));
        gc.fillOval(centerX - radius * 0.5, centerY - radius * 0.5, radius, radius);
    }
}
