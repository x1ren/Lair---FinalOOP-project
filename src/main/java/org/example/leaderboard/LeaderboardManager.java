package org.example.leaderboard;

import javafx.application.Platform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads/sorts leaderboard data and persists new entries on a background thread.
 */
public final class LeaderboardManager {

    private static final LeaderboardManager INSTANCE = new LeaderboardManager();

    private final LeaderboardFileStorage storage = new LeaderboardFileStorage(LeaderboardFileStorage.defaultFilePath());
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "leaderboard-io");
        t.setDaemon(true);
        return t;
    });

    private LeaderboardManager() {
    }

    public static LeaderboardManager get() {
        return INSTANCE;
    }

    public LeaderboardFileStorage storage() {
        return storage;
    }

    /**
     * Sorts by fastest time (lowest millis first).
     */
    public List<LeaderboardEntry> topN(List<LeaderboardEntry> entries, int n) {
        List<LeaderboardEntry> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparingLong(LeaderboardEntry::elapsedMillis));
        if (sorted.size() <= n) {
            return List.copyOf(sorted);
        }
        return List.copyOf(sorted.subList(0, n));
    }

    public void loadAllAsync(java.util.function.Consumer<List<LeaderboardEntry>> onSuccess,
                             Runnable onFailure) {
        ioExecutor.submit(() -> {
            try {
                List<LeaderboardEntry> list = storage.loadAll();
                Platform.runLater(() -> onSuccess.accept(list));
            } catch (IOException e) {
                Platform.runLater(onFailure);
            }
        });
    }

    /**
     * Appends a new entry: load all, add, save atomically via {@link LeaderboardFileStorage#saveAll}.
     */
    public synchronized void submitEntryAsync(LeaderboardEntry entry, Runnable onDone) {
        ioExecutor.submit(() -> {
            try {
                List<LeaderboardEntry> all = new ArrayList<>(storage.loadAll());
                all.add(entry);
                storage.saveAll(all);
            } catch (IOException ignored) {
                // local school project: silent failure acceptable
            } finally {
                if (onDone != null) {
                    Platform.runLater(onDone);
                }
            }
        });
    }

    public void shutdown() {
        ioExecutor.shutdown();
    }
}
