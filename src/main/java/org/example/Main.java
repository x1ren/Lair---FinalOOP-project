package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.runtime.GameContext;

public class Main extends Application {

    public static final int    WIDTH  = 1280;
    public static final int    HEIGHT = 720;
    public static final String TITLE  = "THE LAIR";

    @Override
    public void start(Stage stage) {
        GameContext.initialize(stage);
        GameContext.showIntro();
    }

    public static void main(String[] args) { launch(args); }
}
