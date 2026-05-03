package org.example.leaderboard;

/**
 * Tracks elapsed run time in milliseconds. Caller feeds delta seconds from the game loop;
 * accumulation stops when {@link #stop()} is called or when {@link #accumulate(double)} is not invoked.
 */
public final class RunTimer {

    private boolean started;
    private boolean stopped;
    private long elapsedMillis;

    /**
     * Adds {@code deltaTimeSeconds} to the run only if the clock has started and not stopped.
     */
    public void accumulate(double deltaTimeSeconds) {
        if (!started || stopped || deltaTimeSeconds <= 0) {
            return;
        }
        elapsedMillis += (long) (deltaTimeSeconds * 1000.0);
    }

    /**
     * Marks the run clock as active (idempotent).
     */
    public void start() {
        if (stopped) {
            return;
        }
        started = true;
    }

    /**
     * Freezes the elapsed value for this run.
     */
    public void stop() {
        stopped = true;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isStopped() {
        return stopped;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }
}
