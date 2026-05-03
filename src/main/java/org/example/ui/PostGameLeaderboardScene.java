package org.example.ui;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.Main;
import org.example.app.GameContext;
import org.example.leaderboard.LeaderboardEntry;
import org.example.leaderboard.LeaderboardManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Shown after the story ending; lists fastest runs and highlights the run just completed.
 */
public class PostGameLeaderboardScene {

    private static final int W = Main.WIDTH;
    private static final int H = Main.HEIGHT;
    private static final double PIXEL = 4;
    private static final int TOP_N = 10;

    private static final double BTN_W = 320;
    private static final double BTN_H = 52;
    private static final double BTN_X = W / 2.0 - BTN_W / 2;
    private static final double BTN_Y = H - 100;

    private final Scene scene;
    private final Canvas canvas = new Canvas(W, H);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final LeaderboardEntry highlight;

    private List<LeaderboardEntry> rows = List.of();
    private boolean loadFailed;

    public PostGameLeaderboardScene(LeaderboardEntry highlightRun) {
        this.highlight = highlightRun;
        Pane root = new Pane(canvas);
        scene = new Scene(root, W, H);
        scene.setCursor(javafx.scene.Cursor.DEFAULT);

        scene.setOnMouseClicked(e -> {
            if (isInside(e.getX(), e.getY(), BTN_X, BTN_Y, BTN_W, BTN_H)) {
                GameContext.showCharacterSelect();
            }
        });

        LeaderboardManager.get().loadAllAsync(
                list -> {
                    List<LeaderboardEntry> merged = new ArrayList<>(list);
                    if (highlight != null && merged.stream().noneMatch(highlight::equals)) {
                        merged.add(highlight);
                    }
                    rows = LeaderboardManager.get().topN(merged, TOP_N);
                    render();
                },
                () -> {
                    loadFailed = true;
                    rows = List.of();
                    render();
                });
        render();
    }

    private void render() {
        gc.setFill(Color.color(0.01, 0.02, 0.03));
        gc.fillRect(0, 0, W, H);

        gc.setFill(Color.color(0.08, 0.22, 0.14, 0.35));
        for (int i = 0; i < 10; i++) {
            double sx = (i * 154 + 40) % W;
            gc.fillRect(sx, 0, 4, H);
        }

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 28));
        gc.setFill(Color.color(0.15, 0.9, 0.42));
        String heading = "RUN COMPLETE — LEADERBOARD";
        gc.fillText(heading, W / 2.0 - computeW(heading, 28) / 2, 72);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.color(0.55, 0.68, 0.62));
        String sub = loadFailed ? "Could not load scores from disk." : "Fastest completion times (local)";
        gc.fillText(sub.toUpperCase(), W / 2.0 - computeW(sub, 12) / 2, 104);

        double tableX = W / 2.0 - 380;
        double tableY = 150;
        double tableW = 760;
        double tableH = 420;

        drawPixelPanel(tableX, tableY, tableW, tableH, Color.color(0.02, 0.06, 0.06, 0.96),
                Color.color(0.12, 0.62, 0.38));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.42, 0.58, 0.52));
        double colRank = tableX + 32;
        double colTime = tableX + 88;
        double colName = tableX + 240;
        double colWhen = tableX + 420;
        gc.fillText("RANK", colRank, tableY + 36);
        gc.fillText("TIME", colTime, tableY + 36);
        gc.fillText("PLAYER", colName, tableY + 36);
        gc.fillText("COMPLETED (UTC)", colWhen, tableY + 36);

        double rowY = tableY + 64;
        if (rows.isEmpty() && !loadFailed) {
            gc.setFill(Color.color(0.55, 0.60, 0.62));
            gc.fillText("No entries yet.", colRank, rowY);
        } else {
            int rank = 1;
            for (LeaderboardEntry e : rows) {
                boolean hi = highlight != null && highlight.equals(e);
                if (hi) {
                    gc.setFill(Color.color(0.12, 0.35, 0.18, 0.85));
                    gc.fillRect(tableX + 12, rowY - 14, tableW - 24, 20);
                }
                gc.setFill(hi ? Color.color(0.2, 0.95, 0.55) : Color.color(0.82, 0.88, 0.90));
                gc.fillText(String.valueOf(rank), colRank, rowY);
                gc.fillText(formatDuration(e.elapsedMillis()), colTime, rowY);
                gc.fillText(truncate(e.username(), 22), colName, rowY);
                gc.fillText(e.completedAt().toString().replace("T", " ").replace("Z", " Z"), colWhen, rowY);
                rowY += 22;
                rank++;
                if (rowY > tableY + tableH - 16) {
                    break;
                }
            }
        }

        drawPixelPanel(BTN_X, BTN_Y, BTN_W, BTN_H, Color.color(0.04, 0.14, 0.08, 0.96),
                Color.color(0.15, 0.88, 0.42));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        gc.setFill(Color.color(0.15, 0.9, 0.42));
        String btn = "CONTINUE TO MENU";
        gc.fillText(btn, BTN_X + BTN_W / 2 - computeW(btn, 18) / 2, BTN_Y + 34);
    }

    private static String formatDuration(long millis) {
        long t = millis / 1000;
        long m = t / 60;
        long s = t % 60;
        long ms = millis % 1000;
        return String.format("%d:%02d.%03d", m, s, ms);
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
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
