package org.example;

import org.example.scenes.IntroScene;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static final int    WIDTH  = 1280;
    public static final int    HEIGHT = 720;
    public static final String TITLE  = "THE LAIR";

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle(TITLE);
        stage.setResizable(false);

        // Flow: IntroScene → CharacterSelectScene → WeaponSelectScene → GameScene
        IntroScene intro = new IntroScene();
        stage.setScene(intro.getScene());
        stage.show();
    }

    public static void setScene(javafx.scene.Scene scene) {
        primaryStage.setScene(scene);
    }

    public static Stage getStage() { return primaryStage; }

    public static void main(String[] args) { launch(args); }
}