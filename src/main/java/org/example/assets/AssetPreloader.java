package org.example.assets;

import javafx.application.Platform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AssetPreloader {

    private final AssetRegistry assets;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread worker = new Thread(runnable, "lair-asset-preloader");
        worker.setDaemon(true);
        return worker;
    });

    private volatile boolean started;

    public AssetPreloader(AssetRegistry assets) {
        this.assets = assets;
    }

    public synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        executor.submit(() -> {
            assets.preloadAll();
            Platform.runLater(assets::markPreloadComplete);
        });
    }
}
