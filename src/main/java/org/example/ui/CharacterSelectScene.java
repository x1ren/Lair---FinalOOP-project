package org.example.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.Main;
import org.example.assets.SpriteSheet;
import org.example.leaderboard.LeaderboardEntry;
import org.example.leaderboard.LeaderboardManager;
import org.example.player.CharacterType;
import org.example.app.GameContext;

import java.util.List;

public class CharacterSelectScene {

    private static final int W = Main.WIDTH;
    private static final int H = Main.HEIGHT;
    private static final double PIXEL = 4;

    private static final int CARD_COUNT = 5;
    private static final double CARD_W = 180;
    private static final double CARD_H = 296;
    private static final double CARD_GAP = 16;
    private static final double CARD_ROW_W =
            CARD_COUNT * CARD_W + (CARD_COUNT - 1) * CARD_GAP;
    /** Reserve left column for leaderboard + username (cards stay centered in remaining width). */
    private static final double LEFT_COLUMN_W = 248;
    private static final double CARDS_START_X =
            LEFT_COLUMN_W + (W - LEFT_COLUMN_W - 32 - CARD_ROW_W) / 2.0;
    private static final double CARDS_Y = 164;

    private static final double BTN_W = 300;
    private static final double BTN_H = 56;
    private static final double BTN_X = W / 2.0 - BTN_W / 2;
    private static final double BTN_Y = 642;

    private static final int LEADERBOARD_TOP_N = 10;
    private static final double LB_X = 16;
    private static final double LB_Y = 176;
    private static final double LB_W = 220;
    private static final double LB_H = 440;

    private final Scene scene;
    private final Canvas canvas = new Canvas(W, H);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final TextField usernameField;
    private final AnimationTimer loop;

    private CharacterType selected = CharacterType.JOSEPH_JIMENEZ;
    private int hoveredIndex = -1;
    private double elapsed;
    private double bgPulse;
    private String usernameError = "";
    private List<LeaderboardEntry> leaderboardRows = List.of();

    public CharacterSelectScene() {
        usernameField = new TextField();
        usernameField.setPromptText("Enter your name");
        usernameField.setLayoutX(16);
        usernameField.setLayoutY(92);
        usernameField.setPrefWidth(228);
        usernameField.setPrefHeight(30);
        usernameField.setStyle(
                "-fx-background-color: #050a0a; -fx-control-inner-background: #0a1214;"
                        + " -fx-text-fill: #dceee8; -fx-font-family: Monospace; -fx-font-size: 12px;"
                        + " -fx-border-color: #1a6640; -fx-border-width: 2px;");

        Pane root = new Pane(canvas, usernameField);
        scene = new Scene(root, W, H);
        scene.setCursor(javafx.scene.Cursor.DEFAULT);

        scene.setOnMouseMoved(e -> hoveredIndex = getCardAt(e.getX(), e.getY()));
        scene.setOnMouseClicked(e -> {
            int idx = getCardAt(e.getX(), e.getY());
            if (idx >= 0) {
                selected = CharacterType.values()[idx];
            }
            if (isInside(e.getX(), e.getY(), BTN_X, BTN_Y, BTN_W, BTN_H)) {
                proceedToGame();
            }
        });

        LeaderboardManager.get().loadAllAsync(
                list -> leaderboardRows = LeaderboardManager.get().topN(list, LEADERBOARD_TOP_N),
                () -> leaderboardRows = List.of());

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
                bgPulse = (Math.sin(elapsed * 1.8) + 1) / 2.0;
                render();
            }
        };
        loop.start();
    }

    private void render() {
        gc.setFill(Color.color(0.01, 0.02, 0.03));
        gc.fillRect(0, 0, W, H);
        renderGrid();
        renderVeins();
        renderHeader();
        renderUsernameHint();
        renderLeaderboardPanel();

        CharacterType[] chars = CharacterType.values();
        for (int i = 0; i < chars.length; i++) {
            double cx = CARDS_START_X + i * (CARD_W + CARD_GAP);
            renderCard(chars[i], cx, CARDS_Y, chars[i] == selected, i == hoveredIndex);
        }

        renderLorePanel();
        renderConfirmButton();
    }

    private void renderHeader() {
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(Color.color(0.15, 0.9, 0.4, 0.7));
        String sub = "THE LAIR";
        gc.fillText(sub, W / 2.0 - computeW(sub, 14) / 2, 52);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 34));
        gc.setFill(Color.WHITE);
        String heading = "Choose Your Character";
        gc.fillText(heading.toUpperCase(), W / 2.0 - computeW(heading, 34) / 2, 96);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.color(0.55, 0.72, 0.66));
        String hint = "Choose the survivor and enter with their fixed weapon loadout.";
        gc.fillText(hint.toUpperCase(), W / 2.0 - computeW(hint, 12) / 2, 124);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.45, 0.72, 0.58));
        gc.fillText("NAME (required before play)", 16, 84);

        String preload = GameContext.assets().isPreloadComplete() ? "ASSET SYNC COMPLETE" : "SYNCING SPRITES + AUDIO";
        gc.setFill(GameContext.assets().isPreloadComplete()
                ? Color.color(0.20, 0.90, 0.45)
                : Color.color(0.92, 0.82, 0.18));
        gc.fillText(preload, W - computeW(preload, 12) - 24, 40);

        Image preview = GameContext.assets().image("character.preview");
        if (preview != null) {
            gc.setImageSmoothing(false);
            gc.drawImage(preview, 74, 36, 48, 48);
        }
    }

    private void renderCard(CharacterType character, double x, double y, boolean isSelected, boolean isHovered) {
        double lift = isSelected ? -10 : (isHovered ? -4 : 0);
        double cardY = y + lift;
        Color border = isSelected
                ? Color.color(0.12, 0.90, 0.45)
                : (isHovered ? Color.color(0.35, 0.60, 0.48) : Color.color(0.14, 0.18, 0.24));
        drawPixelPanel(x, cardY, CARD_W, CARD_H,
                isSelected ? Color.color(0.03, 0.11, 0.08, 0.96) : Color.color(0.03, 0.05, 0.07, 0.92),
                border);

        double avatarY = cardY + 18;
        double avatarH = 100;
        gc.setFill(Color.color(0.04, 0.08, 0.08));
        gc.fillRect(snap(x + 16), snap(avatarY), CARD_W - 32, avatarH);
        renderCharacterPreview(character, x + CARD_W / 2.0, avatarY + 6, isSelected);

        double textY = avatarY + avatarH + 24;
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(isSelected ? Color.WHITE : Color.color(0.82, 0.84, 0.86));
        String[] nameParts = character.getName().split(" ");
        for (int i = 0; i < nameParts.length; i++) {
            gc.fillText(nameParts[i], x + CARD_W / 2 - computeW(nameParts[i], 14) / 2, textY + i * 16);
        }

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(isSelected ? Color.color(0.15, 0.9, 0.4, 0.9) : Color.color(0.5, 0.5, 0.55));
        String title = character.getTitle().toUpperCase();
        gc.fillText(title, x + CARD_W / 2 - computeW(title, 11) / 2, textY + 56);

        double statY = textY + 82;
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
        gc.setFill(Color.color(0.38, 0.62, 0.52));
        gc.fillText("LOADOUT", x + CARD_W / 2 - computeW("LOADOUT", 10) / 2, statY);

        renderMiniBar("HP", x + 12, statY + 14, CARD_W - 24, character.getHealth() / 200.0, isSelected);
        renderMiniBar("DMG", x + 12, statY + 30, CARD_W - 24, character.getDamage() / 250.0, isSelected);
        renderMiniBar("MOV", x + 12, statY + 46, CARD_W - 24, character.getMovementSpeed() / 130.0, isSelected);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
        gc.setFill(Color.color(0.58, 0.66, 0.72));
        String weaponLabel = character.getAssignedWeaponLabel().toUpperCase();
        gc.fillText(weaponLabel, x + CARD_W / 2 - computeW(weaponLabel, 10) / 2, statY + 66);

        if (isSelected) {
            gc.setFill(Color.color(0.15, 0.9, 0.4));
            gc.fillRect(snap(x + CARD_W - 88), snap(cardY + 12), 68, 20);
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
            gc.setFill(Color.BLACK);
            gc.fillText("SELECTED", x + CARD_W - 80, cardY + 27);
        }

        renderSkillIconMini(character, x + 12, cardY + 14, 40, 28);
    }

    private void renderCharacterPreview(CharacterType character, double centerX, double topY, boolean selectedCard) {
        SpriteSheet sheet = GameContext.assets().sheet(character.getSpriteAssetId(), 32, 32);
        if (sheet != null) {
            int frame = ((int) Math.floor(elapsed * 6)) % 8;
            sheet.drawFrame(gc, 0, frame, centerX - 32, topY, 64, 64, false);
            return;
        }

        gc.setFill(selectedCard ? Color.color(0.08, 0.82, 0.32) : Color.color(0.28, 0.32, 0.38));
        gc.fillOval(centerX - 24, topY + 8, 48, 48);
    }

    private void renderMiniBar(String label, double x, double y, double maxW, double fill, boolean active) {
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 9));
        gc.setFill(Color.color(0.52, 0.58, 0.64));
        gc.fillText(label, x, y + 9);

        double barX = x + 32;
        double barW = maxW - 36;
        gc.setFill(Color.color(0.08, 0.10, 0.12));
        gc.fillRect(snap(barX), snap(y), barW, 10);

        gc.setFill(active ? Color.color(0.15, 0.85, 0.4, 0.9) : Color.color(0.3, 0.5, 0.35, 0.6));
        gc.fillRect(snap(barX), snap(y), barW * fill, 10);
    }

    private void renderUsernameHint() {
        if (usernameError == null || usernameError.isEmpty()) {
            return;
        }
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.95, 0.35, 0.28));
        gc.fillText(usernameError, 16, 134);
    }

    private void renderLeaderboardPanel() {
        drawPixelPanel(LB_X, LB_Y, LB_W, LB_H, Color.color(0.02, 0.06, 0.06, 0.96),
                Color.color(0.12, 0.55, 0.38));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
        gc.setFill(Color.color(0.15, 0.88, 0.42));
        String title = "FASTEST RUNS (TOP " + LEADERBOARD_TOP_N + ")";
        gc.fillText(title, LB_X + LB_W / 2 - computeW(title, 13) / 2, LB_Y + 28);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
        gc.setFill(Color.color(0.42, 0.55, 0.50));
        gc.fillText("#", LB_X + 18, LB_Y + 56);
        gc.fillText("TIME", LB_X + 44, LB_Y + 56);
        gc.fillText("PLAYER", LB_X + 118, LB_Y + 56);

        double rowY = LB_Y + 76;
        if (leaderboardRows.isEmpty()) {
            gc.setFill(Color.color(0.45, 0.50, 0.52));
            gc.fillText("No runs yet.", LB_X + 18, rowY);
            return;
        }

        int rank = 1;
        for (LeaderboardEntry e : leaderboardRows) {
            gc.setFill(Color.color(0.78, 0.84, 0.86));
            gc.fillText(String.valueOf(rank), LB_X + 18, rowY);
            gc.fillText(formatDuration(e.elapsedMillis()), LB_X + 44, rowY);
            String name = truncate(e.username(), 14);
            gc.fillText(name, LB_X + 118, rowY);
            rowY += 18;
            rank++;
            if (rowY > LB_Y + LB_H - 20) {
                break;
            }
        }
    }

    private static String formatDuration(long millis) {
        long t = millis / 1000;
        long m = t / 60;
        long s = t % 60;
        long ms = millis % 1000;
        return String.format("%d:%02d.%03d", m, s, ms);
    }

    private static String truncate(String s, int maxChars) {
        if (s.length() <= maxChars) {
            return s;
        }
        return s.substring(0, maxChars - 1) + "…";
    }

    private void renderLorePanel() {
        double panelY = 494;
        double panelH = 104;

        drawPixelPanel(88, panelY, W - 176, panelH, Color.color(0.02, 0.05, 0.05, 0.96),
                Color.color(0.10, 0.66, 0.38));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(Color.color(0.86, 0.92, 0.88));
        String roleLine = selected.getTitle().toUpperCase() + " | HP " + selected.getHealth()
                + " | DMG " + selected.getDamage()
                + " | MOV " + selected.getMovementSpeed()
                + " | GUN " + selected.getAssignedWeaponLabel().toUpperCase();
        gc.fillText(roleLine, W / 2.0 - computeW(roleLine, 14) / 2, panelY + 28);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.color(0.72, 0.78, 0.80));
        gc.fillText(selected.getLore().toUpperCase(), W / 2.0 - computeW(selected.getLore(), 12) / 2, panelY + 54);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.color(0.15, 0.9, 0.4, 0.9));
        String skillLine = "SKILL " + selected.getSkillName().toUpperCase() + " | COOLDOWN "
                + (int) selected.getSkillCooldown() + "S";
        gc.fillText(skillLine, W / 2.0 - computeW(skillLine, 12) / 2, panelY + 82);

        double skillIconX = W - 88 - 112;
        renderSkillIconMini(selected, skillIconX, panelY + 18, 104, 73);
    }

    private void renderConfirmButton() {
        drawPixelPanel(BTN_X, BTN_Y, BTN_W, BTN_H, Color.color(0.04, 0.16, 0.08, 0.96),
                Color.color(0.15, 0.9, 0.4, 0.9));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        gc.setFill(Color.color(0.15, 0.9, 0.4));
        String btnText = "ENTER THE LAIR >";
        gc.fillText(btnText, BTN_X + BTN_W / 2 - computeW(btnText, 18) / 2, BTN_Y + 33);
    }

    private void renderGrid() {
        gc.setFill(Color.color(0.02, 0.07, 0.09));
        for (int row = 0; row < 18; row++) {
            for (int col = 0; col < 32; col++) {
                if ((row + col) % 3 == 0) {
                    gc.fillRect(col * 40, row * 40, 40, 40);
                }
                if ((row + col) % 8 == 0) {
                    gc.setFill(Color.color(0.08, 0.25, 0.18, 0.25));
                    gc.fillRect(col * 40 + 8, row * 40 + 8, 12, 12);
                    gc.setFill(Color.color(0.02, 0.07, 0.09));
                }
            }
        }
    }

    private void renderVeins() {
        gc.setFill(Color.color(0.1, 0.5, 0.2, 0.05 + bgPulse * 0.04));
        for (int i = 0; i < 8; i++) {
            double sx = (i * 154 + 40) % W;
            gc.fillRect(sx, 0, 4, H);
            double sy = (i * 112 + 30) % H;
            gc.fillRect(0, sy, W, 4);
        }
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

    private int getCardAt(double mx, double my) {
        CharacterType[] chars = CharacterType.values();
        for (int i = 0; i < chars.length; i++) {
            double cx = CARDS_START_X + i * (CARD_W + CARD_GAP);
            if (isInside(mx, my, cx, CARDS_Y - 15, CARD_W, CARD_H + 20)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isInside(double mx, double my, double x, double y, double width, double height) {
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    private void proceedToGame() {
        String raw = usernameField.getText() == null ? "" : usernameField.getText().trim();
        if (raw.isEmpty()) {
            usernameError = "Enter a name to start.";
            return;
        }
        if (raw.contains(",")) {
            usernameError = "Name cannot contain commas.";
            return;
        }
        usernameError = "";
        loop.stop();
        GameContext.showGame(selected, raw);
    }

    private double computeW(String text, double size) {
        return text.length() * size * 0.52;
    }

    private double snap(double value) {
        return Math.round(value / PIXEL) * PIXEL;
    }

    private void renderSkillIconMini(CharacterType character, double x, double y, double drawW, double drawH) {
        var img = GameContext.assets().image(character.getSkillIconAssetId());
        character.getSkillIconSpec().render(gc, img, snap(x), snap(y), drawW, drawH, elapsed);
    }

    public Scene getScene() {
        return scene;
    }
}
