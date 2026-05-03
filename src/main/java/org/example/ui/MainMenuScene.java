package org.example.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.Main;
import org.example.app.GameContext;
import org.example.leaderboard.LeaderboardEntry;
import org.example.leaderboard.LeaderboardManager;

import java.util.List;

/**
 * Main menu after the title card: display name, leaderboard, continue to story intro.
 */
public class MainMenuScene {

    private static final int W = Main.WIDTH;
    private static final int H = Main.HEIGHT;
    private static final double PIXEL = 4;
    private static final int TOP_N = 10;

    private static final double CONTINUE_BTN_W = 340;
    private static final double CONTINUE_BTN_H = 52;
    private static final double CONTINUE_BTN_X = W / 2.0 - CONTINUE_BTN_W / 2;
    private static final double CONTINUE_BTN_Y = H - 92;

    private static final double BACK_W = 140;
    private static final double BACK_H = 36;
    private static final double BACK_X = 24;
    private static final double BACK_Y = H - 56;

    private static final double FIELD_X = W / 2.0 - 180;
    private static final double FIELD_Y = 162;

    private final Scene scene;
    private final Canvas canvas = new Canvas(W, H);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final TextField nameField;
    private final AnimationTimer loop;

    private double elapsed;
    private double bgPulse;
    private List<LeaderboardEntry> leaderboardRows = List.of();
    private String nameError = "";

    public MainMenuScene() {
        nameField = new TextField();
        nameField.setPromptText("Your display name");
        nameField.setLayoutX(FIELD_X);
        nameField.setLayoutY(FIELD_Y);
        nameField.setPrefWidth(360);
        nameField.setPrefHeight(30);
        nameField.setStyle(
                "-fx-background-color: #050a0a; -fx-control-inner-background: #0a1214;"
                        + " -fx-text-fill: #e8f4ef; -fx-font-family: Monospace; -fx-font-size: 12px;"
                        + " -fx-border-color: #1e7050; -fx-border-width: 2px;");

        Pane root = new Pane(canvas, nameField);
        scene = new Scene(root, W, H);
        scene.setCursor(javafx.scene.Cursor.DEFAULT);

        loop = new AnimationTimer() {
            private long lastTime;

            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    render();
                    return;
                }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                elapsed += dt;
                bgPulse = (Math.sin(elapsed * 1.6) + 1) / 2.0;
                render();
            }
        };

        scene.setOnMouseClicked(e -> {
            if (isInside(e.getX(), e.getY(), BACK_X, BACK_Y, BACK_W, BACK_H)) {
                loop.stop();
                GameContext.showTitleScreen();
                return;
            }
            if (isInside(e.getX(), e.getY(), CONTINUE_BTN_X, CONTINUE_BTN_Y, CONTINUE_BTN_W, CONTINUE_BTN_H)) {
                tryContinueToStory();
            }
        });

        LeaderboardManager.get().loadAllAsync(
                list -> leaderboardRows = LeaderboardManager.get().topN(list, TOP_N),
                () -> leaderboardRows = List.of());

        loop.start();
    }

    private void tryContinueToStory() {
        String raw = nameField.getText() == null ? "" : nameField.getText().trim();
        if (raw.isEmpty()) {
            nameError = "Enter a name to continue.";
            return;
        }
        if (raw.contains(",")) {
            nameError = "Commas are not allowed.";
            return;
        }
        nameError = "";
        GameContext.setSessionPlayerName(raw);
        loop.stop();
        GameContext.showIntro();
    }

    private void render() {
        gc.setFill(Color.color(0.01, 0.02, 0.03));
        gc.fillRect(0, 0, W, H);

        gc.setFill(Color.color(0.08, 0.22, 0.14, 0.22 + bgPulse * 0.06));
        for (int i = 0; i < 9; i++) {
            double sx = (i * 154 + 40) % W;
            gc.fillRect(sx, 0, 3, H);
        }

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
        gc.setFill(Color.color(0.15, 0.88, 0.42));
        String brand = "THE LAIR";
        gc.fillText(brand, W / 2.0 - computeW(brand, 13) / 2, 52);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 36));
        gc.setFill(Color.WHITE);
        String title = "MAIN MENU";
        gc.fillText(title, W / 2.0 - computeW(title, 36) / 2, 100);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.52, 0.68, 0.62));
        gc.fillText("DISPLAY NAME", FIELD_X, FIELD_Y - 10);

        if (!nameError.isEmpty()) {
            gc.setFill(Color.color(0.95, 0.38, 0.30));
            gc.fillText(nameError, FIELD_X, FIELD_Y + 46);
        }

        double panelW = 540;
        double panelH = 268;
        double panelX = W / 2.0 - panelW / 2;
        double panelY = 228;
        drawPixelPanel(panelX, panelY, panelW, panelH,
                Color.color(0.02, 0.06, 0.06, 0.96),
                Color.color(0.12, 0.58, 0.36));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.42, 0.56, 0.50));
        double cx = panelX + 28;
        gc.fillText("FASTEST CLEARS — TOP " + TOP_N, cx, panelY + 26);
        gc.fillText("#", cx, panelY + 48);
        gc.fillText("TIME", cx + 36, panelY + 48);
        gc.fillText("PLAYER", cx + 200, panelY + 48);

        double rowY = panelY + 68;
        if (leaderboardRows.isEmpty()) {
            gc.setFill(Color.color(0.50, 0.56, 0.58));
            gc.fillText("No completed runs yet.", cx, rowY);
        } else {
            int rank = 1;
            for (LeaderboardEntry e : leaderboardRows) {
                gc.setFill(Color.color(0.82, 0.88, 0.90));
                gc.fillText(String.valueOf(rank), cx, rowY);
                gc.fillText(LeaderboardFormat.formatDuration(e.elapsedMillis()), cx + 36, rowY);
                gc.fillText(LeaderboardFormat.truncate(e.username(), 20), cx + 200, rowY);
                rowY += 20;
                rank++;
                if (rowY > panelY + panelH - 12) {
                    break;
                }
            }
        }

        drawPixelPanel(CONTINUE_BTN_X, CONTINUE_BTN_Y, CONTINUE_BTN_W, CONTINUE_BTN_H,
                Color.color(0.04, 0.14, 0.08, 0.96),
                Color.color(0.15, 0.88, 0.42));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 17));
        gc.setFill(Color.color(0.15, 0.92, 0.44));
        String go = "CONTINUE TO STORY";
        gc.fillText(go, CONTINUE_BTN_X + CONTINUE_BTN_W / 2 - computeW(go, 17) / 2, CONTINUE_BTN_Y + 33);

        drawPixelPanel(BACK_X, BACK_Y, BACK_W, BACK_H,
                Color.color(0.03, 0.06, 0.07, 0.94),
                Color.color(0.28, 0.42, 0.38));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.78, 0.84, 0.86));
        String back = "BACK TO TITLE";
        gc.fillText(back, BACK_X + BACK_W / 2 - computeW(back, 11) / 2, BACK_Y + 24);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        String preload = GameContext.assets().isPreloadComplete() ? "ASSET SYNC COMPLETE" : "SYNCING SPRITES + AUDIO";
        gc.setFill(GameContext.assets().isPreloadComplete()
                ? Color.color(0.20, 0.90, 0.45)
                : Color.color(0.92, 0.82, 0.18));
        gc.fillText(preload, W - computeW(preload, 11) - 20, 36);
    }

    private void drawPixelPanel(double x, double y, double width, double height, Color bg, Color border) {
        x = snap(x);
        y = snap(y);
        width = snap(width);
        height = snap(height);
        gc.setFill(border);
        gc.fillRect(x, y, width, height);
        gc.setFill(Color.color(0.02, 0.03, 0.03));
        gc.fillRect(x + PIXEL, y + PIXEL, width - PIXEL * 2, height - PIXEL * 2);
        gc.setFill(bg);
        gc.fillRect(x + PIXEL * 2, y + PIXEL * 2, width - PIXEL * 4, height - PIXEL * 4);
    }

    private double snap(double v) {
        return Math.round(v / PIXEL) * PIXEL;
    }

    private double computeW(String text, double size) {
        return text.length() * size * 0.52;
    }

    private static boolean isInside(double mx, double my, double x, double y, double w, double h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public Scene getScene() {
        return scene;
    }
}
