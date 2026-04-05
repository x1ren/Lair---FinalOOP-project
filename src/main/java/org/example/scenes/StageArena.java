package org.example.scenes;

import javafx.scene.image.Image;
import org.example.gameplay.PlatformTile;
import org.example.gameplay.PlayerActor;
import org.example.gameplay.StageDefinition;
import org.example.gameplay.StageExitMarker;

import java.util.ArrayList;
import java.util.List;

final class StageArena {

    private final double viewportWidth;
    private final double groundY;
    private final StageExitMarker exitMarker;
    private final List<PlatformTile> platforms = new ArrayList<>();

    private double worldWidth;
    private double cameraX;

    StageArena(double viewportWidth, double groundY) {
        this.viewportWidth = viewportWidth;
        this.groundY = groundY;
        this.exitMarker = new StageExitMarker(viewportWidth - 92, groundY - 84, 44, 64);
    }

    void prepareStage(StageDefinition stage, int stageIndex, PlayerActor player, Image backdrop) {
        platforms.clear();
        worldWidth = computeWorldWidth(backdrop);
        cameraX = 0;
        buildStageLayout(stageIndex);

        player.setX(120);
        player.setY(groundY - player.getHeight());
        player.setVx(0);
        player.setVy(0);
        player.setOnGround(true);

        exitMarker.setX(worldWidth - 92);
        exitMarker.setY(groundY - 84);
    }

    List<PlatformTile> platforms() {
        return platforms;
    }

    StageExitMarker exitMarker() {
        return exitMarker;
    }

    double worldWidth() {
        return worldWidth;
    }

    double cameraX() {
        return cameraX;
    }

    void updateCamera(PlayerActor player) {
        double target = player.getCenterX() - viewportWidth / 2.0;
        cameraX += (target - cameraX) * 0.12;
        cameraX = clamp(cameraX, 0, Math.max(0, worldWidth - viewportWidth));
    }

    void clampPlayer(PlayerActor player) {
        player.clampX(0, worldWidth - player.getWidth());
    }

    double mobSpawnX(int index, int total) {
        double laneStart = Math.max(520, worldWidth * 0.40);
        double laneWidth = Math.max(280, worldWidth * 0.45);
        double spacing = laneWidth / Math.max(1, total - 1);
        return laneStart + index * spacing;
    }

    double bossSpawnX(boolean finalStage) {
        return worldWidth - (finalStage ? 420 : 320);
    }

    private double computeWorldWidth(Image backdrop) {
        double targetH = groundY - 24 - 64;
        if (backdrop == null || backdrop.getHeight() <= 0) {
            return viewportWidth * 1.75;
        }
        return Math.max(viewportWidth, targetH * (backdrop.getWidth() / backdrop.getHeight()));
    }

    private void buildStageLayout(int stageIndex) {
        double w = worldWidth;
        double lowY = 520;
        double midY = 456;
        double highY = 392;

        platforms.add(new PlatformTile(140, lowY, 240, 18));
        platforms.add(new PlatformTile(w * 0.28, midY, 210, 18));
        platforms.add(new PlatformTile(w * 0.48, highY, 210, 18));
        platforms.add(new PlatformTile(w * 0.68, midY + 24, 200, 18));

        if (stageIndex == 0) {
            platforms.add(new PlatformTile(w * 0.84, 420, 170, 18));
        } else if (stageIndex == 1) {
            platforms.add(new PlatformTile(w * 0.80, 370, 180, 18));
            platforms.add(new PlatformTile(w * 0.58, 512, 170, 18));
        } else if (stageIndex == 2) {
            platforms.add(new PlatformTile(w * 0.75, 338, 200, 18));
        } else {
            platforms.add(new PlatformTile(w * 0.55, 350, 190, 18));
            platforms.add(new PlatformTile(w * 0.86, 460, 180, 18));
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
