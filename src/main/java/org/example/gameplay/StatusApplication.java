package org.example.gameplay;

/**
 * One status to apply on hit. Bleed uses {@code magnitude} as total DoT pool; slow uses it as duration (seconds).
 */
public record StatusApplication(StatusType type, double magnitude) {
}
