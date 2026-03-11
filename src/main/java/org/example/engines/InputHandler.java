package org.example.engines;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import java.util.HashSet;
import java.util.Set;

/**
 * InputHandler tracks all keyboard and mouse state each frame.
 *
 * Usage:
 *   InputHandler input = new InputHandler();
 *   input.attachTo(scene);
 *
 *   // In update():
 *   if (input.isDown(KeyCode.A)) { ... move left }
 *   if (input.isMouseClicked()) { shoot toward input.getMouseX(), input.getMouseY() }
 */
public class InputHandler {

    // Keys currently held down
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    // Keys pressed THIS frame only (for one-shot actions)
    private final Set<KeyCode> justPressedKeys = new HashSet<>();
    private final Set<KeyCode> justReleasedKeys = new HashSet<>();

    // Mouse state
    private double mouseX = 0;
    private double mouseY = 0;
    private boolean mouseLeftDown = false;
    private boolean mouseLeftJustClicked = false;
    private boolean mouseRightJustClicked = false;

    public void attachTo(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (!pressedKeys.contains(e.getCode())) {
                justPressedKeys.add(e.getCode());
            }
            pressedKeys.add(e.getCode());
        });

        scene.setOnKeyReleased(e -> {
            pressedKeys.remove(e.getCode());
            justReleasedKeys.add(e.getCode());
        });

        scene.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        scene.setOnMouseDragged(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        scene.setOnMousePressed(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
            if (e.getButton() == MouseButton.PRIMARY) {
                mouseLeftDown = true;
                mouseLeftJustClicked = true;
            }
            if (e.getButton() == MouseButton.SECONDARY) {
                mouseRightJustClicked = true;
            }
        });

        scene.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                mouseLeftDown = false;
            }
        });
    }

    /**
     * Call this at the END of each frame to clear one-shot states.
     */
    public void endFrame() {
        justPressedKeys.clear();
        justReleasedKeys.clear();
        mouseLeftJustClicked = false;
        mouseRightJustClicked = false;
    }

    // ── Keyboard ──────────────────────────────────────────────

    /** True while the key is held down */
    public boolean isDown(KeyCode key) {
        return pressedKeys.contains(key);
    }

    /** True only on the frame the key was first pressed */
    public boolean isJustPressed(KeyCode key) {
        return justPressedKeys.contains(key);
    }

    /** True only on the frame the key was released */
    public boolean isJustReleased(KeyCode key) {
        return justReleasedKeys.contains(key);
    }

    // ── Mouse ─────────────────────────────────────────────────

    public double getMouseX() { return mouseX; }
    public double getMouseY() { return mouseY; }

    /** True while left mouse button is held */
    public boolean isMouseLeftDown() { return mouseLeftDown; }

    /** True only on the frame left mouse was clicked */
    public boolean isMouseLeftJustClicked() { return mouseLeftJustClicked; }

    /** True only on the frame right mouse was clicked */
    public boolean isMouseRightJustClicked() { return mouseRightJustClicked; }
}