package org.example.scenes;



import org.example.Main;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
        import javafx.scene.paint.Color;
import javafx.scene.text.*;

/**
 * Simple main menu. "Play" goes to character select.
 */
public class MenuScene {

    private final Scene scene;

    public MenuScene() {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #11111f;");

        Text title = new Text("PLATFORMER");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 64));
        title.setFill(Color.WHITE);

        Text subtitle = new Text("2D Action Game");
        subtitle.setFont(Font.font("Arial", 20));
        subtitle.setFill(Color.LIGHTGRAY);

        Button playBtn = styledButton("▶  PLAY");
        playBtn.setOnAction(e -> {
            CharacterSelectScene charSelect = new CharacterSelectScene();
            Main.setScene(charSelect.getScene());
        });

        Button quitBtn = styledButton("QUIT");
        quitBtn.setOnAction(e -> System.exit(0));

        root.getChildren().addAll(title, subtitle, playBtn, quitBtn);
        scene = new Scene(root, Main.WIDTH, Main.HEIGHT);
    }

    private Button styledButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("""
            -fx-background-color: #2a2a4a;
            -fx-border-color: #6666aa;
            -fx-border-width: 2;
            -fx-padding: 12 40 12 40;
            -fx-cursor: hand;
            """);
        btn.setOnMouseEntered(e -> btn.setStyle("""
            -fx-background-color: #4a4a8a;
            -fx-border-color: #aaaaff;
            -fx-border-width: 2;
            -fx-padding: 12 40 12 40;
            -fx-cursor: hand;
            """));
        btn.setOnMouseExited(e -> btn.setStyle("""
            -fx-background-color: #2a2a4a;
            -fx-border-color: #6666aa;
            -fx-border-width: 2;
            -fx-padding: 12 40 12 40;
            -fx-cursor: hand;
            """));
        return btn;
    }

    public Scene getScene() { return scene; }
}