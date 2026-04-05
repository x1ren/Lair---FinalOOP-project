package org.example.assets;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public final class SpriteSheet {

    private final Image image;
    private final int frameWidth;
    private final int frameHeight;
    private final int columns;

    public SpriteSheet(Image image, int frameWidth, int frameHeight) {
        this.image = image;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.columns = Math.max(1, (int) image.getWidth() / frameWidth);
    }

    public int columns() {
        return columns;
    }

    public void drawFrame(GraphicsContext gc, int row, int column,
                          double x, double y, double width, double height,
                          boolean flipX) {
        gc.save();
        gc.setImageSmoothing(false);
        if (flipX) {
            gc.translate(x + width, y);
            gc.scale(-1, 1);
            x = 0;
            y = 0;
        }
        gc.drawImage(
                image,
                column * frameWidth,
                row * frameHeight,
                frameWidth,
                frameHeight,
                x,
                y,
                width,
                height
        );
        gc.restore();
    }
}
