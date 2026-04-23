package org.example.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.example.assets.AssetRegistry;
import org.example.gameplay.PlayerActor;
import org.example.gameplay.StageDefinition;
import org.example.weapons.Weapon;
import org.example.weapons.WeaponType;

final class GameVisualRenderer {

    private final GraphicsContext gc;
    private final AssetRegistry assets;
    private final double viewportWidth;
    private final double viewportHeight;
    private final double groundY;

    GameVisualRenderer(GraphicsContext gc, AssetRegistry assets, double viewportWidth, double viewportHeight, double groundY) {
        this.gc = gc;
        this.assets = assets;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.groundY = groundY;
    }

    void renderBackground(StageDefinition stage, Image backdrop, double cameraX, double worldWidth) {
        gc.setFill(Color.color(0.01, 0.02, 0.03));
        gc.fillRect(0, 0, viewportWidth, viewportHeight);

        if (backdrop != null) {
            drawBackdropAsRoom(backdrop, cameraX, worldWidth);
        } else {
            gc.setFill(Color.color(0.03, 0.08, 0.11));
            gc.fillRect(0, 0, viewportWidth, viewportHeight - 96);
        }

        gc.setFill(Color.color(stage.tint().getRed(), stage.tint().getGreen(), stage.tint().getBlue(), 0.08));
        gc.fillRect(0, 72, viewportWidth, groundY - 72);

        gc.setFill(Color.color(0, 0, 0, 0.10));
        gc.fillRect(0, 0, viewportWidth, 68);

        gc.setFill(Color.color(0.01, 0.02, 0.03, 0.18));
        gc.fillRect(0, groundY - 36, viewportWidth, 36);

        gc.setFill(Color.color(0.03, 0.05, 0.06, 0.55));
        gc.fillRect(0, groundY, viewportWidth, viewportHeight - groundY);
    }

    void renderPlayerWeapon(PlayerActor player, Weapon weapon, boolean finished, boolean victory,
                            double aimAngle, double muzzleFlashTimer) {
        if (finished && !victory) {
            return;
        }

        boolean aimingRight = Math.cos(aimAngle) >= 0;
        double shoulderX = player.getCenterX() + (aimingRight ? 10 : -10);
        double shoulderY = player.getY() + player.getHeight() * 0.38;

        gc.save();
        gc.translate(shoulderX, shoulderY);
        gc.rotate(Math.toDegrees(aimAngle));

        gc.setFill(Color.color(0, 0, 0, 0.18));
        gc.fillRect(4, -2, 26, 6);

        if (weapon.getType() == WeaponType.SMG) {
            renderSmgWeaponSprite(muzzleFlashTimer);
        } else {
            renderProceduralWeapon(weapon.getType());
        }

        gc.restore();
    }

    void renderMuzzleFlash(double muzzleFlashTimer, double muzzleFlashX, double muzzleFlashY, double muzzleFlashAngle) {
        if (muzzleFlashTimer <= 0) {
            return;
        }

        Image flashSheet = assets.image("effect.muzzle_flash");
        if (flashSheet == null) {
            return;
        }

        int frameWidth = 32;
        int frameHeight = 64;
        int columns = (int) flashSheet.getWidth() / frameWidth;
        int column = Math.min(columns - 1, (int) ((0.08 - muzzleFlashTimer) / 0.08 * columns));

        gc.save();
        gc.translate(muzzleFlashX, muzzleFlashY);
        gc.rotate(Math.toDegrees(muzzleFlashAngle));
        gc.setImageSmoothing(false);
        gc.drawImage(flashSheet, column * frameWidth, 0, frameWidth, frameHeight, 0, -14, 48, 36);
        gc.restore();
    }

    private void drawBackdropAsRoom(Image backdrop, double cameraX, double worldWidth) {
        gc.setImageSmoothing(false);

        double targetX = 0;
        double targetY = 64;
        double targetW = viewportWidth;
        double targetH = groundY - 24 - targetY;
        double scaledWorldWidth = targetH * (backdrop.getWidth() / backdrop.getHeight());
        double scrollRatio = scaledWorldWidth <= targetW ? 0 : cameraX / (scaledWorldWidth - targetW);
        double sourceW = backdrop.getWidth() * (targetW / scaledWorldWidth);
        double sourceX = scrollRatio * (backdrop.getWidth() - sourceW);

        gc.drawImage(backdrop, sourceX, 0, sourceW, backdrop.getHeight(), targetX, targetY, targetW, targetH);
        gc.setFill(Color.color(0, 0, 0, 0.08));
        gc.fillRect(targetX, targetY, targetW, targetH);
    }

    private void renderSmgWeaponSprite(double muzzleFlashTimer) {
        Image smgSheet = assets.image("weapon.smg");
        if (smgSheet == null) {
            renderProceduralWeapon(WeaponType.SMG);
            return;
        }

        int frameWidth = 64;
        int frameHeight = 64;
        int columns = Math.max(1, (int) (smgSheet.getWidth() / frameWidth));
        int column;
        if (muzzleFlashTimer <= 0 || columns <= 1) {
            column = 0;
        } else {
            int flashFrames = columns - 1;
            double flashDuration = 0.08;
            int idx = (int) ((flashDuration - muzzleFlashTimer) / flashDuration * flashFrames);
            idx = Math.min(flashFrames - 1, Math.max(0, idx));
            column = 1 + idx;
        }

        gc.setImageSmoothing(false);
        gc.drawImage(smgSheet, column * frameWidth, 0, frameWidth, frameHeight,
                -6, -20, 56, 32);
    }

    private void renderProceduralWeapon(WeaponType weaponType) {
        Color body = switch (weaponType) {
            case ASSAULT_RIFLE -> Color.color(0.34, 0.40, 0.46);
            case SHOTGUN -> Color.color(0.56, 0.34, 0.16);
            case SNIPER -> Color.color(0.74, 0.74, 0.78);
            case LMG -> Color.color(0.44, 0.22, 0.22);
            case SMG -> Color.color(0.34, 0.48, 0.26);
        };

        gc.setFill(body);
        switch (weaponType) {
            case ASSAULT_RIFLE -> {
                gc.fillRect(0, -5, 30, 8);
                gc.fillRect(12, -9, 10, 4);
                gc.fillRect(8, 3, 5, 8);
                gc.fillRect(26, -3, 8, 3);
            }
            case SHOTGUN -> {
                gc.fillRect(0, -4, 26, 7);
                gc.fillRect(18, -2, 15, 2);
                gc.fillRect(4, 3, 6, 11);
            }
            case SNIPER -> {
                gc.fillRect(0, -4, 36, 6);
                gc.fillRect(10, -9, 12, 3);
                gc.fillRect(6, 2, 5, 10);
                gc.fillRect(32, -2, 12, 2);
            }
            case LMG -> {
                gc.fillRect(0, -5, 32, 8);
                gc.fillRect(10, -9, 12, 4);
                gc.fillRect(6, 3, 6, 10);
                gc.fillRect(14, 5, 12, 3);
                gc.fillRect(28, -3, 10, 3);
            }
            case SMG -> {
                gc.fillRect(0, -4, 24, 7);
                gc.fillRect(6, 3, 4, 8);
                gc.fillRect(20, -2, 8, 2);
            }
        }

        gc.setFill(Color.color(0.92, 0.96, 1.0, 0.22));
        gc.fillRect(2, -3, 8, 2);
    }
}
