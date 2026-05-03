package org.example.weapons;

public enum WeaponType {
    ASSAULT_RIFLE,
    SMG,
    SHOTGUN,
    SNIPER,
    LMG;

    /**
     * Barrel tip along local +X from the shoulder pivot (matches {@link org.example.ui.GameVisualRenderer} gun art).
     */
    public double muzzleTipLocalX() {
        return switch (this) {
            case ASSAULT_RIFLE -> 34;
            case SHOTGUN -> 33;
            case SNIPER -> 44;
            case LMG -> 38;
            case SMG -> 48;
        };
    }

    /** Barrel tip lateral offset in gun-local space before rotation (+Y is down on screen). */
    public double muzzleTipLocalY() {
        return switch (this) {
            case ASSAULT_RIFLE, SHOTGUN, SNIPER, LMG -> -1;
            case SMG -> -6;
        };
    }
}