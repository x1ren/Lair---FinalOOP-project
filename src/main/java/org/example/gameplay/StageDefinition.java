package org.example.gameplay;

import javafx.scene.paint.Color;

public record StageDefinition(
        String name,
        String objective,
        String description,
        int enemyCount,
        int enemyHealth,
        double enemySpeed,
        Color tint,
        boolean boss,
        String bossName
) { }
