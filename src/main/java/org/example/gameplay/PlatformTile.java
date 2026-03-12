package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PlatformTile extends GameObject {

    public PlatformTile(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public void render(GraphicsContext gc) {
        double x = Math.round(getX());
        double y = Math.round(getY());
        double tile = 8;

        gc.setFill(Color.color(0.16, 0.20, 0.24));
        gc.fillRect(x, y, getWidth(), getHeight());
        gc.setFill(Color.color(0.26, 0.32, 0.36));
        for (double px = x; px < x + getWidth(); px += tile * 2) {
            gc.fillRect(px, y, tile, tile);
        }
        gc.setFill(Color.color(0.09, 0.12, 0.14));
        gc.fillRect(x, y + getHeight() - 4, getWidth(), 4);
    }
}
