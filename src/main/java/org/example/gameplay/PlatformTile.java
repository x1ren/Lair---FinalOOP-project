package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PlatformTile extends GameObject {

    public PlatformTile(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.setFill(Color.color(0.18, 0.2, 0.22));
        gc.fillRoundRect(getX(), getY(), getWidth(), getHeight(), 8, 8);
    }
}
