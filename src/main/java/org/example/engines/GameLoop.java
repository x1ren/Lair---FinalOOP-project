package org.example.engines;


import javafx.animation.AnimationTimer;

/**
 * GameLoop drives the entire game using JavaFX's AnimationTimer.
 * Subclass this and implement update() and render().
 *
 * Uses deltaTime (seconds since last frame) so game speed is
 * frame-rate independent.
 */
public abstract class GameLoop extends AnimationTimer {

    private long lastTime = 0;
    private boolean running = false;

    // FPS tracking
    private int frameCount = 0;
    private long fpsTimer = 0;
    private int currentFPS = 0;

    @Override
    public final void handle(long now) {
        if (lastTime == 0) {
            lastTime = now;
            return;
        }

        double deltaTime = (now - lastTime) / 1_000_000_000.0;
        lastTime = now;

        // Cap delta to avoid huge jumps if window loses focus
        if (deltaTime > 0.05) deltaTime = 0.05;

        // FPS counter
        frameCount++;
        fpsTimer += (now - lastTime + (long)(deltaTime * 1_000_000_000));
        if (fpsTimer >= 1_000_000_000L) {
            currentFPS = frameCount;
            frameCount = 0;
            fpsTimer = 0;
        }

        update(deltaTime);
        render();
    }

    /**
     * Called every frame. Update all game logic here.
     * @param deltaTime seconds since last frame (~0.0167 at 60fps)
     */
    protected abstract void update(double deltaTime);

    /**
     * Called every frame after update(). Draw everything here.
     */
    protected abstract void render();

    @Override
    public void start() {
        running = true;
        lastTime = 0;
        super.start();
    }

    @Override
    public void stop() {
        running = false;
        super.stop();
    }

    public boolean isRunning() { return running; }
    public int getCurrentFPS() { return currentFPS; }
}