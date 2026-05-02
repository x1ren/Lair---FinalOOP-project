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
                        "Silent stacks, flickering lights, and infected footsteps close in from the shelves.",
                        "Infected Librarian",
                        5,
                        72,
                        78,
                        List.of("enemy.librarian"),
                        "Security Guard",
                        260,
                        135, // Increased from 90 to 135 for faster mobility
                        "enemy.security_guard",
                        "stage.library",
                        Color.color(0.34, 0.42, 0.52)
                ),
                new StageDefinition(
                        "Stage 2 - Canteen",
                        "Break through the canteen and defeat the Mutated Vendor.",
                        "Overturned tables and corrupted kitchen waste turn the canteen into a chaotic kill zone.",
                        "The food court is still lit, but everything moving inside it wants you dead.",
                        "Infected Student",
                        6,
                        82,
                        88,
                        List.of("enemy.student_f", "enemy.student_m"),
                        "Mutated Vendor",
                        320,
                        96,
                        "enemy.vendor",
                        "stage.canteen",
                        Color.color(0.56, 0.34, 0.18)
                ),
                new StageDefinition(
                        "Stage 3 - Gym",
                        "Survive the gas-filled gym and defeat Caesar Hunos.",
                        "The gym is thick with LAIR gas. Infected janitors roam the floor while Caesar waits at the center.",
                        "Bleachers loom overhead while the infected gas turns the court into a trap.",
                        "Infected Janitor",
                        6,
                        90,
                        92,
                        List.of("enemy.janitor"),
                        "Caesar Hunos",
                        450,
                        102,
                        "enemy.caesar_hunos",
                        "stage.gym",
                        Color.color(0.68, 0.18, 0.18)
                ),
                new StageDefinition(
                        "Stage 4 - Final Confrontation",
                        "Face Khai's true form and end the nightmare.",
                        "The gym floor cracks as Khai's monstrous form emerges. Spikes rain from above and erupt from below.",
                        "The air is thick with dread. Khai stands motionless, but his attacks are relentless.",
                        "",
                        0,
                        0,
                        0,
                        List.of(),
                        "Khai (Boss Form)",
                        650,
                        0,
                        "enemy.khai_boss_form",
                        "stage.covered_court",
                        Color.color(0.28, 0.12, 0.32)
                )
        );
    }
}
