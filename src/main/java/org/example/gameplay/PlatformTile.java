package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PlatformTile extends GameObject {

    public PlatformTile(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public void render(GraphicsContext gc) {
        renderBase(gc);
        renderTop(gc);
    }

    public void renderBase(GraphicsContext gc) {
        double x = Math.round(getX());
        double y = Math.round(getY());
        double tile = 8;

        // Shadow
        gc.setFill(Color.color(0.06, 0.12, 0.14, 0.22));
        gc.fillRect(x - 6, y - 6, getWidth() + 12, getHeight() + 16);

        // Platform body
        gc.setFill(Color.color(0.18, 0.22, 0.24, 0.96));
        gc.fillRect(x, y, getWidth(), getHeight());
        
        // Top green line
        gc.setFill(Color.color(0.12, 0.86, 0.42, 0.85));
        gc.fillRect(x, y, getWidth(), 4);

        // Tile pattern
        gc.setFill(Color.color(0.30, 0.36, 0.40));
        for (double px = x; px < x + getWidth(); px += tile * 2) {
            gc.fillRect(px, y, tile, tile);
        }

        // Bottom dark line
        gc.setFill(Color.color(0.07, 0.09, 0.11));
        gc.fillRect(x, y + getHeight() - 4, getWidth(), 4);

        // Legs
        gc.setFill(Color.color(0.05, 0.08, 0.09, 0.72));
        for (double px = x + 10; px < x + getWidth(); px += 36) {
            gc.fillRect(px, y + getHeight(), 6, 18);
        }
    }

    public void renderTop(GraphicsContext gc) {
        // Empty - not used anymore
    }
}
