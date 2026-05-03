package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents a health potion that can be picked up by the player.
 * Potions have a 10% drop rate when enemies are defeated.
 */
public class Potion extends GameObject {

    private static final double FLOAT_AMPLITUDE = 8.0;
    private static final double FLOAT_SPEED = 3.0;
    private static final double GLOW_PULSE_SPEED = 4.0;
    
    private final double baseY;
    private double animationTime;

    public Potion(double x, double y) {
        super(x, y, 24, 24);
        this.baseY = y;
        this.animationTime = 0;
    }

    public void update(double dt) {
        animationTime += dt;
        // Floating animation
        double floatOffset = Math.sin(animationTime * FLOAT_SPEED) * FLOAT_AMPLITUDE;
        setY(baseY + floatOffset);
    }

    @Override
    public void render(GraphicsContext gc) {
        double x = Math.round(getX());
        double y = Math.round(getY());
        double w = getWidth();
        double h = getHeight();
        
        // Pulsing glow effect
        double glowAlpha = 0.3 + 0.2 * Math.sin(animationTime * GLOW_PULSE_SPEED);
        gc.setFill(Color.color(0.95, 0.2, 0.2, glowAlpha));
        gc.fillOval(x - 6, y - 6, w + 12, h + 12);
        
        // Outer bottle shape (dark red glass)
        gc.setFill(Color.color(0.5, 0.1, 0.1, 0.9));
        gc.fillRoundRect(x + 4, y + 6, w - 8, h - 6, 4, 4);
        
        // Cork/cap
        gc.setFill(Color.color(0.6, 0.4, 0.2));
        gc.fillRect(x + 6, y + 2, w - 12, 6);
        
        // Liquid inside (bright red)
        gc.setFill(Color.color(0.95, 0.2, 0.2, 0.85));
        gc.fillRoundRect(x + 6, y + 10, w - 12, h - 14, 3, 3);
        
        // Highlight/shine
        gc.setFill(Color.color(1.0, 0.8, 0.8, 0.6));
        gc.fillOval(x + 8, y + 12, 4, 6);
        
        // Small sparkle
        double sparkleAlpha = 0.5 + 0.5 * Math.sin(animationTime * GLOW_PULSE_SPEED * 1.5);
        gc.setFill(Color.color(1.0, 1.0, 1.0, sparkleAlpha));
        gc.fillRect(x + w - 8, y + 8, 2, 2);
    }
}
