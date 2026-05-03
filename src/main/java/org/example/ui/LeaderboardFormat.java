package org.example.ui;

/** Shared formatting for leaderboard rows (UI layers only). */
public final class LeaderboardFormat {

    private LeaderboardFormat() {
    }

    public static String formatDuration(long millis) {
        long t = millis / 1000;
        long m = t / 60;
        long s = t % 60;
        long ms = millis % 1000;
        return String.format("%d:%02d.%03d", m, s, ms);
    }

    public static String truncate(String s, int maxChars) {
        if (s.length() <= maxChars) {
            return s;
        }
        return s.substring(0, maxChars - 1) + "…";
    }
}
