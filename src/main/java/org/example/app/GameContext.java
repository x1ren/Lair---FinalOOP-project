package org.example.app;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.Main;
import org.example.assets.AssetPreloader;
import org.example.assets.AssetRegistry;
import org.example.audio.AudioManager;
import org.example.player.CharacterType;
import org.example.leaderboard.LeaderboardEntry;
import org.example.ui.CharacterSelectScene;
import org.example.ui.EndingScene;
import org.example.ui.GameScene;
import org.example.ui.IntroScene;
import org.example.ui.MainMenuScene;
import org.example.ui.PostGameLeaderboardScene;

public final class GameContext {

    private static Stage stage;
    private static AssetRegistry assets;
    private static AudioManager audio;
    private static AssetPreloader preloader;

    /** Display name for the current run; set on main menu, cleared when returning to main menu. */
    private static String sessionPlayerName;

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

    /** Opening title card (same as before); advances to main menu. */
    public static void showTitleScreen() {
        switchScene(new IntroScene().getScene());
    }

    public static void showMainMenu() {
        sessionPlayerName = null;
        switchScene(new MainMenuScene().getScene());
    }

    /** Full dialogue intro after main menu; ends at character select. */
    public static void showIntro() {
        switchScene(IntroScene.createStoryIntro().getScene());
    }

    public static void showCharacterSelect() {
        switchScene(new CharacterSelectScene().getScene());
    }

    public static void showGame(CharacterType character) {
        String name = sessionPlayerName;
        if (name == null || name.isBlank()) {
            showMainMenu();
            return;
        }
        switchScene(new GameScene(character, name).getScene());
    }

    public static void showEnding(LeaderboardEntry completedRun) {
        switchScene(new EndingScene(completedRun).getScene());
    }

    public static void showPostGameLeaderboard(LeaderboardEntry highlightRun) {
        switchScene(new PostGameLeaderboardScene(highlightRun).getScene());
    }

    public static void switchScene(Scene scene) {
        stage.setScene(scene);
        stage.show();
    }

    public static void setSessionPlayerName(String name) {
        sessionPlayerName = name;
    }

    public static AssetRegistry assets() {
        return assets;
    }

    public static AudioManager audio() {
        return audio;
    }
}
