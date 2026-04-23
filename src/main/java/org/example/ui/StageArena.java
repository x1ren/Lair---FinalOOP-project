package org.example.ui;

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
    private int currentStageIndex;

    StageArena(double viewportWidth, double groundY) {
        this.viewportWidth = viewportWidth;
        this.groundY = groundY;
        this.exitMarker = new StageExitMarker(viewportWidth - 92, groundY - 84, 44, 64);
    }

    void prepareStage(StageDefinition stage, int stageIndex, PlayerActor player, Image backdrop) {
        platforms.clear();
        worldWidth = computeWorldWidth(backdrop);
        cameraX = 0;
        currentStageIndex = stageIndex;
        buildStageLayout(stageIndex);

        // Get the floor Y position for this stage
        double floorY = getFloorY(stageIndex);

        // Position player on the main floor at the start
        player.setX(100);  // Start position near the left edge
        player.setY(floorY - player.getHeight());  // On top of the main floor platform
        player.setVx(0);
        player.setVy(0);
        player.setOnGround(true);

        // Position exit marker on the floor at the end of the stage
        exitMarker.setX(worldWidth - 92);
        exitMarker.setY(floorY - exitMarker.getHeight());  // On top of the floor tiles
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

    private double getFloorY(int stageIndex) {
        // Different floor heights for different stages based on their tile positions
        if (stageIndex == 0) {
            // Stage 1 - Library: tiles are higher
            return 500;
        } else if (stageIndex == 1) {
            // Stage 2 - Canteen: tiles are higher (same as library)
            return 500;
        } else if (stageIndex == 2) {
            // Stage 3 - Gym: tiles are at the bottom
            return 580;
        } else {
            // Stage 4 - Courtyard: tiles are higher
            return 520;
        }
    }

    private void buildStageLayout(int stageIndex) {
        double w = worldWidth;
        double floorY = getFloorY(stageIndex);
        
        // Create a continuous floor platform that spans the entire stage width
        double floorWidth = w - 40;  // Leave small margins on edges
        platforms.add(new PlatformTile(20, floorY, floorWidth, 18));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
