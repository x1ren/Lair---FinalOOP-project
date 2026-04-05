package org.example.assets;

import javafx.scene.canvas.GraphicsContext;

import java.util.EnumMap;

public final class SpriteSet {

    private final SpriteSheet sheet;
    private final EnumMap<AnimationState, AnimationStrip> strips = new EnumMap<>(AnimationState.class);

    private SpriteSet(SpriteSheet sheet) {
        this.sheet = sheet;
    }

    public static SpriteSet player(SpriteSheet sheet) {
        SpriteSet set = new SpriteSet(sheet);
        set.state(AnimationState.IDLE, new AnimationStrip(0, 0, 8, 6));
        set.state(AnimationState.WALK, new AnimationStrip(1, 0, 4, 8));
        set.state(AnimationState.JUMP, new AnimationStrip(2, 0, 3, 6));
        set.state(AnimationState.FALL, new AnimationStrip(2, 4, 4, 7));
        set.state(AnimationState.HIT, new AnimationStrip(3, 0, 1, 1));
        set.state(AnimationState.DEATH, new AnimationStrip(4, 0, 6, 8));
        return set;
    }

    public SpriteSet state(AnimationState state, AnimationStrip strip) {
        strips.put(state, strip);
        return this;
    }

    public void draw(GraphicsContext gc, AnimationState state, double elapsedSeconds,
                     double x, double y, double width, double height, boolean flipX) {
        AnimationStrip strip = strips.getOrDefault(state, strips.get(AnimationState.IDLE));
        if (strip == null) {
            return;
        }
        sheet.drawFrame(gc, strip.row(), strip.frameAt(elapsedSeconds), x, y, width, height, flipX);
    }
}
