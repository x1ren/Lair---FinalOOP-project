package org.example.app;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.Main;
import org.example.assets.AssetPreloader;
import org.example.assets.AssetRegistry;
import org.example.audio.AudioManager;
import org.example.player.CharacterType;
import org.example.ui.CharacterSelectScene;
import org.example.ui.GameScene;
import org.example.ui.IntroScene;

public final class GameContext {

    private static Stage stage;
    private static AssetRegistry assets;
    private static AudioManager audio;
    private static AssetPreloader preloader;

    private GameContext() {
    }

    public static void initialize(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle(Main.TITLE);
        stage.setResizable(false);

        assets = new AssetRegistry();
        audio = new AudioManager(assets);
        preloader = new AssetPreloader(assets);
        preloader.start();
    }

    public static void showIntro() {
        switchScene(new IntroScene().getScene());
    }

    public static void showCharacterSelect() {
        switchScene(new CharacterSelectScene().getScene());
    }

    public static void showGame(CharacterType character) {
        switchScene(new GameScene(character).getScene());
    }

    public static void switchScene(Scene scene) {
        stage.setScene(scene);
        stage.show();
    }

    public static AssetRegistry assets() {
        return assets;
    }

    public static AudioManager audio() {
        return audio;
    }
}
