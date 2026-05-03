package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.app.GameContext;
import org.example.app.LaunchConfig;
import org.example.gameplay.StageCatalog;

public class Main extends Application {

    public static final int    WIDTH  = 1280;
    public static final int    HEIGHT = 720;
    public static final String TITLE  = "THE LAIR";

    @Override
    public void start(Stage stage) {
        LaunchConfig.parseApplicationParameters(getParameters());
        GameContext.initialize(stage);
        if (LaunchConfig.isDebugStageLaunch()) {
            int n = StageCatalog.buildStoryStages().size();
            System.err.println("[LaunchConfig] Debug jump: stage=" + LaunchConfig.debugStage1BasedDisplay()
                    + " (1.." + n + "), player=" + LaunchConfig.debugPlayerName()
                    + ", character=" + LaunchConfig.debugCharacter());
            GameContext.enterGameFromDebugShortcut(n);
        } else {
            GameContext.showTitleScreen();
        }
    }

    public static void main(String[] args) {
        LaunchConfig.bootstrapFromMain(args);
        launch(args);
    }
}
