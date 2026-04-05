package org.example.gameplay;

import javafx.scene.paint.Color;

import java.util.List;

public record StageDefinition(
        String name,
        String objective,
        String description,
        String moodText,
        String enemyName,
        int enemyCount,
        int enemyHealth,
        double enemySpeed,
        List<String> enemySpriteIds,
        String bossName,
        int bossHealth,
        double bossSpeed,
        String bossSpriteId,
        String backdropAssetId,
        Color tint
) {
    public boolean hasMobs() {
        return enemyCount > 0;
    }

    public boolean hasBoss() {
        return bossName != null && !bossName.isBlank();
    }
}
