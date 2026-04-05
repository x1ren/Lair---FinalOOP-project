package org.example.assets;

public record AnimationStrip(int row, int startColumn, int frameCount, double fps) {

    public int frameAt(double elapsedSeconds) {
        if (frameCount <= 1 || fps <= 0) {
            return startColumn;
        }
        int offset = (int) Math.floor(elapsedSeconds * fps) % frameCount;
        return startColumn + offset;
    }
}
