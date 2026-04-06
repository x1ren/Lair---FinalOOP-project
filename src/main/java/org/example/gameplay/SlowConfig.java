package org.example.gameplay;

/** Optional diminishing returns on repeated slow (Iben / Suppress). */
public record SlowConfig(int diminishAfterStacks, double diminishFactor) {
    public static SlowConfig none() {
        return new SlowConfig(Integer.MAX_VALUE, 1.0);
    }
}
