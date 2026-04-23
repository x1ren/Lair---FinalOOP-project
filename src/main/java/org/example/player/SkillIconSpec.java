package org.example.player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Describes how each survivor's signature skill PNG is laid out in its strip.
 * Values come from the non-transparent content region of each asset (not the raw 140px canvas).
 */
public record SkillIconSpec(
        int cropX,
        int cropY,
        int cropWidth,
        int cropHeight,
        int frameWidth,
        int frameHeight,
        int frameCount,
        double framesPerSecond
) {

    /**
     * {@code true} when the art is one continuous panel (no uniform frame grid), e.g. Ilde's sheet width is prime.
     */
    public boolean isBanner() {
        return frameCount <= 1;
    }

    public void render(GraphicsContext gc, Image image,
                       double destX, double destY, double destW, double destH,
                       double animTimeSeconds) {
        if (image == null) {
            return;
        }
        gc.save();
        gc.setImageSmoothing(false);
        if (isBanner()) {
            gc.drawImage(image, cropX, cropY, cropWidth, cropHeight,
                    destX, destY, destW, destH);
        } else {
            int frame = frameCount <= 0 ? 0
                    : (int) Math.floor(animTimeSeconds * framesPerSecond) % frameCount;
            double sx = cropX + frame * (double) frameWidth;
            gc.drawImage(image, sx, cropY, frameWidth, frameHeight,
                    destX, destY, destW, destH);
        }
        gc.restore();
    }
}
