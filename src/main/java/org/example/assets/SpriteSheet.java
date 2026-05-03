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

    public int frameWidth() {
        return frameWidth;
    }

    public int frameHeight() {
        return frameHeight;
    }

    /**
     * Draw a sub-rectangle of one cell (coords relative to the cell's top-left). Useful when frame cells include
     * transparent padding that should not stretch with the hitbox.
     */
    public void drawFramePartial(GraphicsContext gc, int row, int column,
                                 double x, double y, double width, double height,
                                 boolean flipX,
                                 double cropInCellX, double cropInCellY,
                                 double cropW, double cropH) {
        double sx = column * frameWidth + cropInCellX;
        double sy = row * frameHeight + cropInCellY;
        gc.save();
        gc.setImageSmoothing(false);
        if (flipX) {
            gc.translate(x + width, y);
            gc.scale(-1, 1);
            x = 0;
            y = 0;
        }
        gc.drawImage(image, sx, sy, cropW, cropH, x, y, width, height);
        gc.restore();
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
