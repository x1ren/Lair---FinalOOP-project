package org.example.leaderboard;

import java.time.Instant;
import java.util.Objects;

/**
 * One persisted leaderboard row: player name, run duration, and when the run was recorded.
 */
public record LeaderboardEntry(String username, long elapsedMillis, Instant completedAt) {

    public LeaderboardEntry {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(completedAt, "completedAt");
        if (username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (elapsedMillis < 0) {
            throw new IllegalArgumentException("elapsedMillis must be non-negative");
        }
    }
}
