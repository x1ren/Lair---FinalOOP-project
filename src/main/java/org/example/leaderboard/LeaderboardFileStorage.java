package org.example.leaderboard;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes {@link LeaderboardEntry} rows as CSV lines under {@code ~/.lair/leaderboard.csv}.
 * Usernames are restricted to characters that do not require CSV quoting (enforced at input).
 */
public final class LeaderboardFileStorage {

    public static final String FILENAME = "leaderboard.csv";

    private final Path filePath;

    public LeaderboardFileStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path filePath() {
        return filePath;
    }

    public static Path defaultFilePath() {
        String home = System.getProperty("user.home");
        Path dir = Path.of(home, ".lair");
        return dir.resolve(FILENAME);
    }

    public List<LeaderboardEntry> loadAll() throws IOException {
        if (!Files.isRegularFile(filePath)) {
            return List.of();
        }
        List<LeaderboardEntry> out = new ArrayList<>();
        List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            LeaderboardEntry entry = parseLine(line);
            if (entry != null) {
                out.add(entry);
            }
        }
        return out;
    }

    /**
     * Writes the full list (replaces file). Creates parent directories if needed.
     */
    public void saveAll(List<LeaderboardEntry> entries) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = Files.createTempFile(filePath.getParent(), "lb-", ".tmp");
        try {
            try (BufferedWriter w = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                for (LeaderboardEntry e : entries) {
                    w.write(formatLine(e));
                    w.newLine();
                }
            }
            Files.move(temp, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } finally {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
                // best effort
            }
        }
    }

    static String formatLine(LeaderboardEntry e) {
        return e.username() + "," + e.elapsedMillis() + "," + e.completedAt().toString();
    }

    static LeaderboardEntry parseLine(String line) {
        String[] parts = line.split(",", 3);
        if (parts.length != 3) {
            return null;
        }
        try {
            String username = parts[0].trim();
            long millis = Long.parseLong(parts[1].trim());
            Instant completedAt = Instant.parse(parts[2].trim());
            return new LeaderboardEntry(username, millis, completedAt);
        } catch (Exception e) {
            return null;
        }
    }
}
