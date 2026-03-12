package org.example.gameplay;

import javafx.scene.paint.Color;

import java.util.List;

public final class StageCatalog {

    private StageCatalog() {
    }

    public static List<StageDefinition> buildStoryStages() {
        return List.of(
                new StageDefinition(
                        "Stage 1 - Library",
                        "Clear the library and defeat the Security Guard.",
                        "Broken shelves, scattered books, and dark growth choke the library as infected librarians stalk the aisles.",
                        "Infected Librarian",
                        5,
                        72,
                        78,
                        "Security Guard",
                        260,
                        90,
                        Color.color(0.34, 0.42, 0.52)
                ),
                new StageDefinition(
                        "Stage 2 - Canteen",
                        "Break through the canteen and defeat the Mutated Vendor.",
                        "Overturned tables and corrupted kitchen waste turn the canteen into a chaotic kill zone.",
                        "Infected Student",
                        6,
                        82,
                        88,
                        "Mutated Vendor",
                        320,
                        96,
                        Color.color(0.56, 0.34, 0.18)
                ),
                new StageDefinition(
                        "Stage 3 - Gym",
                        "Survive the gas-filled gym and defeat Caesar Hunos.",
                        "The gym is thick with LAIR gas. Infected janitors roam the floor while Caesar waits at the center.",
                        "Infected Janitor",
                        6,
                        90,
                        92,
                        "Caesar Hunos",
                        450,
                        102,
                        Color.color(0.68, 0.18, 0.18)
                ),
                new StageDefinition(
                        "Stage 4 - Courtyard",
                        "Confront the truth and defeat the LAIR Mimic.",
                        "Sir Khai's torn jacket hangs from a tree. The thing wearing his face has stopped pretending.",
                        "",
                        0,
                        0,
                        0,
                        "LAIR Mimic (False Sir Khai)",
                        520,
                        108,
                        Color.color(0.42, 0.70, 0.24)
                )
        );
    }
}
