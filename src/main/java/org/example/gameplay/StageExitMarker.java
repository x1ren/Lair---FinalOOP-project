package org.example.gameplay;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class StageExitMarker extends GameObject {

    private boolean active;
    private String label = "NEXT";

    public StageExitMarker(double x, double y, double width, double height) {
        super(x, y, width, height);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!active) {
            return;
        }

        double x = Math.round(getX());
        double y = Math.round(getY());

        gc.setFill(Color.color(0.12, 0.88, 0.4, 0.16));
        gc.fillRect(x - 8, y - 8, getWidth() + 16, getHeight() + 16);

        gc.setFill(Color.color(0.10, 0.80, 0.34, 0.95));
        gc.fillRect(x, y, getWidth(), getHeight());
        gc.setFill(Color.color(0.03, 0.08, 0.05, 0.95));
        gc.fillRect(x + 4, y + 4, getWidth() - 8, getHeight() - 8);

        double midX = x + getWidth() / 2.0;
        gc.setFill(Color.color(0.14, 0.95, 0.42, 0.95));
        gc.fillRect(midX - 8, y + 8, 16, 28);
        gc.fillPolygon(
                new double[]{midX - 20, midX, midX + 20},
                new double[]{y + 34, y + 58, y + 34},
                3
        );

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.WHITE);
        gc.fillText(label, x - 6, y - 10);
    }
}
