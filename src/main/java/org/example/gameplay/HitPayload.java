package org.example.gameplay;

import java.util.ArrayList;
import java.util.List;

/**
 * Direct damage plus optional status applications from a projectile.
 */
public final class HitPayload {

    private final int directDamage;
    private final List<StatusApplication> statuses;

    public HitPayload(int directDamage, List<StatusApplication> statuses) {
        this.directDamage = directDamage;
        this.statuses = statuses == null || statuses.isEmpty()
                ? List.of()
                : List.copyOf(statuses);
    }

    public static HitPayload ofDamage(int damage) {
        return new HitPayload(damage, List.of());
    }

    public static HitPayload withBleedSlow(int damage, int bleedTotal, double slowDurationSec) {
        List<StatusApplication> list = new ArrayList<>();
        if (bleedTotal > 0) {
            list.add(new StatusApplication(StatusType.BLEED, bleedTotal));
        }
        if (slowDurationSec > 0) {
            list.add(new StatusApplication(StatusType.SLOW, slowDurationSec));
        }
        return new HitPayload(damage, list);
    }

    public int getDirectDamage() {
        return directDamage;
    }

    public List<StatusApplication> getStatuses() {
        return statuses;
    }

    /** Legacy accessors for code paths still using parallel fields. */
    public int bleedTotalOrZero() {
        for (StatusApplication s : statuses) {
            if (s.type() == StatusType.BLEED) {
                return (int) Math.round(s.magnitude());
            }
        }
        return 0;
    }

    public double slowDurationOrZero() {
        for (StatusApplication s : statuses) {
            if (s.type() == StatusType.SLOW) {
                return s.magnitude();
            }
        }
        return 0;
    }
}
