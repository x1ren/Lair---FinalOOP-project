package org.example.scenes;

import org.example.Main;
import org.example.player.CharacterType;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;

/**
 * Character Selection Screen for THE LAIR.
 *
 * Design: dark infected campus aesthetic.
 * Green glowing toxic gas vibe from the LAIR virus.
 */
public class CharacterSelectScene {

    private static final int W = Main.WIDTH;
    private static final int H = Main.HEIGHT;
    private static final double PIXEL = 4;

    private final Scene scene;
    private final Canvas canvas = new Canvas(W, H);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    private CharacterType selected = CharacterType.JOSEPH_JIMENEZ;
    private int hoveredIndex = -1;

    // Card layout
    private static final int   CARD_COUNT  = 5;
    private static final double CARD_W     = 180;
    private static final double CARD_H     = 296;
    private static final double CARD_GAP   = 16;
    private static final double CARDS_START_X =
            (W - (CARD_COUNT * CARD_W + (CARD_COUNT - 1) * CARD_GAP)) / 2.0;
    private static final double CARDS_Y    = 164;

    // Confirm button bounds
    private static final double BTN_W   = 300;
    private static final double BTN_H   = 56;
    private static final double BTN_X   = W / 2.0 - BTN_W / 2;
    private static final double BTN_Y   = 642;

    private double bgPulse = 0;

    public CharacterSelectScene() {
        Pane root = new Pane(canvas);
        scene = new Scene(root, W, H);
        scene.setCursor(javafx.scene.Cursor.DEFAULT);

        scene.setOnMouseMoved(e -> {
            hoveredIndex = getCardAt(e.getX(), e.getY());
        });

        scene.setOnMouseClicked(e -> {
            int idx = getCardAt(e.getX(), e.getY());
            if (idx >= 0) {
                selected = CharacterType.values()[idx];
            }

            // Confirm button
            if (e.getX() >= BTN_X && e.getX() <= BTN_X + BTN_W
                    && e.getY() >= BTN_Y && e.getY() <= BTN_Y + BTN_H) {
                proceedToGame();
            }
        });

        startLoop();
    }

    private void startLoop() {
        AnimationTimer loop = new AnimationTimer() {
            long start = 0;
            @Override public void handle(long now) {
                if (start == 0) start = now;
                double t = (now - start) / 1_000_000_000.0;
                bgPulse = (Math.sin(t * 1.8) + 1) / 2.0;
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

        // ── Cards ─────────────────────────────────────────────
        CharacterType[] chars = CharacterType.values();
        for (int i = 0; i < chars.length; i++) {
            double cx = CARDS_START_X + i * (CARD_W + CARD_GAP);
            boolean isSelected = chars[i] == selected;
            boolean isHovered  = i == hoveredIndex;
            renderCard(gc, chars[i], cx, CARDS_Y, isSelected, isHovered);
        }

        renderLorePanel();
        renderConfirmButton();
    }

    private void renderCard(GraphicsContext gc, CharacterType c,
                            double x, double y,
                            boolean selected, boolean hovered) {

        double lift   = selected ? -10 : (hovered ? -4 : 0);
        double cardY  = y + lift;
        Color border = selected
                ? Color.color(0.12, 0.90, 0.45)
                : (hovered ? Color.color(0.35, 0.60, 0.48) : Color.color(0.14, 0.18, 0.24));
        drawPixelPanel(x, cardY, CARD_W, CARD_H,
                selected ? Color.color(0.03, 0.11, 0.08, 0.96) : Color.color(0.03, 0.05, 0.07, 0.92),
                border);

        double avatarY = cardY + 18;
        double avatarH = 100;
        gc.setFill(Color.color(0.04, 0.08, 0.08));
        gc.fillRect(snap(x + 16), snap(avatarY), CARD_W - 32, avatarH);
        drawPixelSilhouette(x + CARD_W / 2.0, avatarY + 20, selected
                ? Color.color(0.08, 0.82, 0.32)
                : Color.color(0.28, 0.32, 0.38));

        double textY = avatarY + avatarH + 24;
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(selected ? Color.WHITE : Color.color(0.82, 0.84, 0.86));
        String[] nameParts = c.name.split(" ");
        for (int i = 0; i < nameParts.length; i++) {
            gc.fillText(nameParts[i], x + CARD_W / 2 - computeW(nameParts[i], 14) / 2, textY + i * 16);
        }

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(selected
                ? Color.color(0.15, 0.9, 0.4, 0.9)
                : Color.color(0.5, 0.5, 0.55));
        String title = c.title.toUpperCase();
        gc.fillText(title, x + CARD_W / 2 - computeW(title, 11) / 2, textY + 56);

        double statY = textY + 82;
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
        gc.setFill(Color.color(0.38, 0.62, 0.52));
        gc.fillText("LOADOUT", x + CARD_W / 2 - computeW("LOADOUT", 10) / 2, statY);

        renderMiniBar(gc, "HP",  x + 12, statY + 14, CARD_W - 24, c.getHealth() / 200.0, selected);
        renderMiniBar(gc, "DMG", x + 12, statY + 30, CARD_W - 24, c.getDamage() / 250.0, selected);
        renderMiniBar(gc, "MOV", x + 12, statY + 46, CARD_W - 24, c.getMovementSpeed() / 130.0, selected);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
        gc.setFill(Color.color(0.58, 0.66, 0.72));
        gc.fillText(c.getAssignedWeaponLabel().toUpperCase(), x + CARD_W / 2 - computeW(c.getAssignedWeaponLabel(), 10) / 2, statY + 66);

        if (selected) {
            gc.setFill(Color.color(0.15, 0.9, 0.4));
            gc.fillRect(snap(x + CARD_W - 88), snap(cardY + 12), 68, 20);
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
            gc.setFill(Color.BLACK);
            gc.fillText("SELECTED", x + CARD_W - 80, cardY + 27);
        }
    }

    private void renderMiniBar(GraphicsContext gc, String label,
                               double x, double y, double maxW,
                               double fill, boolean active) {
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 9));
        gc.setFill(Color.color(0.52, 0.58, 0.64));
        gc.fillText(label, x, y + 9);

        double barX = x + 32;
        double barW = maxW - 36;

        gc.setFill(Color.color(0.08, 0.10, 0.12));
        gc.fillRect(snap(barX), snap(y), barW, 10);

        gc.setFill(active
                ? Color.color(0.15, 0.85, 0.4, 0.9)
                : Color.color(0.3, 0.5, 0.35, 0.6));
        gc.fillRect(snap(barX), snap(y), barW * fill, 10);
    }

    private void renderLorePanel() {
        double panelY = 494;
        double panelH = 104;

        drawPixelPanel(88, panelY, W - 176, panelH, Color.color(0.02, 0.05, 0.05, 0.96),
                Color.color(0.10, 0.66, 0.38));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(Color.color(0.86, 0.92, 0.88));
        String roleLine = selected.title.toUpperCase() + " | HP " + selected.getHealth()
                + " | DMG " + selected.getDamage()
                + " | MOV " + selected.getMovementSpeed()
                + " | GUN " + selected.getAssignedWeaponLabel().toUpperCase();
        gc.fillText(roleLine, W / 2.0 - computeW(roleLine, 14) / 2, panelY + 28);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.color(0.72, 0.78, 0.80));
        gc.fillText(selected.lore.toUpperCase(), W / 2.0 - computeW(selected.lore, 12) / 2, panelY + 54);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.color(0.15, 0.9, 0.4, 0.9));
        String skillLine = "SKILL " + selected.getSkillName().toUpperCase() + " | COOLDOWN "
                + (int) selected.getSkillCooldown() + "S";
        gc.fillText(skillLine, W / 2.0 - computeW(skillLine, 12) / 2, panelY + 82);
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

    private void drawPixelSilhouette(double centerX, double topY, Color color) {
        double x = snap(centerX - 16);
        double y = snap(topY);
        gc.setFill(color);
        gc.fillRect(x + 8, y, 16, 16);
        gc.fillRect(x + 4, y + 16, 24, 32);
        gc.fillRect(x + 8, y + 48, 6, 16);
        gc.fillRect(x + 18, y + 48, 6, 16);
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
            if (mx >= cx && mx <= cx + CARD_W && my >= CARDS_Y - 15 && my <= CARDS_Y + CARD_H + 5) {
                return i;
            }
        }
        return -1;
    }

    private void proceedToGame() {
        GameScene game = new GameScene(selected);
        Main.setScene(game.getScene());
    }

    private double computeW(String text, double size) {
        return text.length() * size * 0.52;
    }

    private double snap(double value) {
        return Math.round(value / PIXEL) * PIXEL;
    }

    public Scene getScene() { return scene; }
}
