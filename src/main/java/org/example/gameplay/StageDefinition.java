package org.example.gameplay;

import javafx.scene.paint.Color;

public record StageDefinition(
        String name,
        String objective,
        String description,
        String enemyName,
        int enemyCount,
        int enemyHealth,
        double enemySpeed,
        String bossName,
        int bossHealth,
        double bossSpeed,
        Color tint
) {
    public boolean hasMobs() {
        return enemyCount > 0;
    }

    public boolean hasBoss() {
        return bossName != null && !bossName.isBlank();
    }
}
