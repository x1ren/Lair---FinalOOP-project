package org.example.scenes;

import org.example.Main;
import org.example.player.CharacterType;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;

/**
 * Character Selection Screen for THE LAIR.
 *
 * POLYMORPHISM in action:
 *   Each CharacterType is treated uniformly through the enum.
 *   When the player picks one, it's passed polymorphically
 *   to WeaponSelectScene and eventually to Player constructor.
 *
 * Design: dark infected campus aesthetic.
 * Green glowing toxic gas vibe from the LAIR virus.
 */
public class CharacterSelectScene {

    private static final int W = Main.WIDTH;
    private static final int H = Main.HEIGHT;

    private final Scene scene;
    private final Canvas canvas = new Canvas(W, H);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    private CharacterType selected = CharacterType.JOSEPH_JIMENEZ;
    private int hoveredIndex = -1;

    // Card layout
    private static final int   CARD_COUNT  = 5;
    private static final double CARD_W     = 190;
    private static final double CARD_H     = 310;
    private static final double CARD_GAP   = 18;
    private static final double CARDS_START_X =
            (W - (CARD_COUNT * CARD_W + (CARD_COUNT - 1) * CARD_GAP)) / 2.0;
    private static final double CARDS_Y    = 160;

    // Confirm button bounds
    private static final double BTN_W   = 260;
    private static final double BTN_H   = 52;
    private static final double BTN_X   = W / 2.0 - BTN_W / 2;
    private static final double BTN_Y   = H - 90;

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
                proceedToWeaponSelect();
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
        // ── Background ────────────────────────────────────────
        gc.setFill(Color.color(0.03, 0.04, 0.03));
        gc.fillRect(0, 0, W, H);

        // Vein network (atmospheric)
        renderVeins();

        // ── Header ────────────────────────────────────────────
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.15, 0.9, 0.4, 0.5));
        String sub = "— T H E   L A I R —";
        gc.fillText(sub, W / 2.0 - sub.length() * 3.5, 52);

        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 38));
        gc.setFill(Color.WHITE);
        String heading = "Choose Your Character";
        gc.fillText(heading, W / 2.0 - computeW(heading, 38) / 2, 100);

        gc.setFont(Font.font("Georgia", FontPosture.ITALIC, 13));
        gc.setFill(Color.color(0.55, 0.55, 0.55));
        String hint = "Choose the survivor whose aura bonded with LAIR.";
        gc.fillText(hint, W / 2.0 - computeW(hint, 13) / 2, 128);

        // ── Cards ─────────────────────────────────────────────
        CharacterType[] chars = CharacterType.values();
        for (int i = 0; i < chars.length; i++) {
            double cx = CARDS_START_X + i * (CARD_W + CARD_GAP);
            boolean isSelected = chars[i] == selected;
            boolean isHovered  = i == hoveredIndex;
            renderCard(gc, chars[i], cx, CARDS_Y, isSelected, isHovered);
        }

        // ── Selected character lore ───────────────────────────
        renderLorePanel();

        // ── Confirm button ────────────────────────────────────
        renderConfirmButton();
    }

    private void renderCard(GraphicsContext gc, CharacterType c,
                            double x, double y,
                            boolean selected, boolean hovered) {

        double lift   = selected ? -12 : (hovered ? -5 : 0);
        double cardY  = y + lift;
        double alpha  = selected ? 1.0 : (hovered ? 0.85 : 0.65);

        // Card shadow
        if (selected || hovered) {
            Color glowColor = selected
                    ? Color.color(0.15, 0.9, 0.4, 0.25)
                    : Color.color(0.4, 0.5, 0.7, 0.12);
            gc.setFill(glowColor);
            gc.fillRoundRect(x - 8, cardY + 8, CARD_W + 16, CARD_H + 16, 16, 16);
        }

        // Card background
        Color bg = selected
                ? Color.color(0.07, 0.12, 0.08, alpha)
                : Color.color(0.06, 0.06, 0.08, alpha);
        gc.setFill(bg);
        gc.fillRoundRect(x, cardY, CARD_W, CARD_H, 12, 12);

        // Border
        Color border = selected
                ? Color.color(0.15, 0.9, 0.4, 0.9)
                : Color.color(0.25, 0.25, 0.3, 0.5);
        gc.setStroke(border);
        gc.setLineWidth(selected ? 2.0 : 1.0);
        gc.strokeRoundRect(x, cardY, CARD_W, CARD_H, 12, 12);

        // ── Character avatar placeholder ──────────────────────
        double avatarY = cardY + 16;
        double avatarH = 130;
        gc.setFill(Color.color(0.08, 0.10, 0.09));
        gc.fillRoundRect(x + 10, avatarY, CARD_W - 20, avatarH, 8, 8);

        // Placeholder silhouette
        gc.setFill(selected
                ? Color.color(0.15, 0.9, 0.4, 0.5)
                : Color.color(0.3, 0.3, 0.35, 0.4));
        double sx = x + CARD_W / 2;
        double sy = avatarY + avatarH - 5;
        // Body
        gc.fillRect(sx - 14, sy - 70, 28, 50);
        // Head
        gc.fillOval(sx - 13, sy - 90, 26, 26);

        // "PLACEHOLDER" text
        gc.setFont(Font.font("Courier New", 9));
        gc.setFill(Color.color(0.3, 0.3, 0.3));
        gc.fillText("[ ART PENDING ]", x + 18, avatarY + avatarH - 6);

        // ── Name ──────────────────────────────────────────────
        double textY = avatarY + avatarH + 22;
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        gc.setFill(selected ? Color.WHITE : Color.color(0.8, 0.8, 0.8));
        String[] nameParts = c.name.split(" ");
        for (int i = 0; i < nameParts.length; i++) {
            gc.fillText(nameParts[i], x + CARD_W / 2 - computeW(nameParts[i], 14) / 2, textY + i * 16);
        }

        // ── Title ─────────────────────────────────────────────
        gc.setFont(Font.font("Georgia", FontPosture.ITALIC, 11));
        gc.setFill(selected
                ? Color.color(0.15, 0.9, 0.4, 0.9)
                : Color.color(0.5, 0.5, 0.55));
        String title = "\"" + c.title + "\"";
        gc.fillText(title, x + CARD_W / 2 - computeW(title, 11) / 2, textY + 56);

        // ── Base stats bar ────────────────────────────────────
        double statY = textY + 78;
        gc.setFont(Font.font("Courier New", 10));
        gc.setFill(Color.color(0.4, 0.4, 0.45));
        gc.fillText("LAIR STATS", x + CARD_W / 2 - 30, statY);

        renderMiniBar(gc, "HP",   x + 12, statY + 14, CARD_W - 24, c.getHealth() / 100.0, selected);
        renderMiniBar(gc, "LOG",  x + 12, statY + 30, CARD_W - 24, c.getLogic() / 100.0, selected);
        renderMiniBar(gc, "WIS",  x + 12, statY + 46, CARD_W - 24, c.getWisdom() / 100.0, selected);

        gc.setFont(Font.font("Courier New", 9));
        gc.setFill(Color.color(0.35, 0.35, 0.4));
        gc.fillText("Mana: " + c.getMana() + "  Basic: " + c.getBasicAttack(), x + 12, statY + 66);

        // ── Selected badge ────────────────────────────────────
        if (selected) {
            gc.setFill(Color.color(0.15, 0.9, 0.4));
            gc.fillRoundRect(x + CARD_W / 2 - 35, cardY + CARD_H - 26, 70, 18, 6, 6);
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 10));
            gc.setFill(Color.BLACK);
            gc.fillText("SELECTED", x + CARD_W / 2 - 27, cardY + CARD_H - 13);
        }
    }

    private void renderMiniBar(GraphicsContext gc, String label,
                               double x, double y, double maxW,
                               double fill, boolean active) {
        gc.setFont(Font.font("Courier New", 9));
        gc.setFill(Color.color(0.4, 0.4, 0.45));
        gc.fillText(label, x, y + 9);

        double barX = x + 32;
        double barW = maxW - 36;

        gc.setFill(Color.color(0.1, 0.1, 0.12));
        gc.fillRoundRect(barX, y, barW, 10, 4, 4);

        gc.setFill(active
                ? Color.color(0.15, 0.85, 0.4, 0.9)
                : Color.color(0.3, 0.5, 0.35, 0.6));
        gc.fillRoundRect(barX, y, barW * fill, 10, 4, 4);
    }

    private void renderLorePanel() {
        double panelY = CARDS_Y + CARD_H + 18;
        double panelH = 88;

        gc.setFill(Color.color(0.04, 0.06, 0.04, 0.85));
        gc.fillRoundRect(60, panelY, W - 120, panelH, 8, 8);
        gc.setStroke(Color.color(0.15, 0.6, 0.3, 0.3));
        gc.setLineWidth(1);
        gc.strokeRoundRect(60, panelY, W - 120, panelH, 8, 8);

        gc.setFont(Font.font("Georgia", FontPosture.ITALIC, 13));
        gc.setFill(Color.color(0.7, 0.75, 0.7));
        String roleLine = selected.title + "  |  HP " + selected.getHealth()
                + "  |  Logic " + selected.getLogic()
                + "  |  Wisdom " + selected.getWisdom()
                + "  |  Mana " + selected.getMana();
        gc.fillText(roleLine, W / 2.0 - computeW(roleLine, 13) / 2, panelY + 22);

        gc.setFont(Font.font("Georgia", 12));
        gc.setFill(Color.color(0.82, 0.82, 0.82));
        gc.fillText(selected.lore, W / 2.0 - computeW(selected.lore, 12) / 2, panelY + 45);

        gc.setFont(Font.font("Courier New", 11));
        gc.setFill(Color.color(0.15, 0.9, 0.4, 0.85));
        String skillLine = "Skills: " + selected.getSkillSummary();
        gc.fillText(skillLine, W / 2.0 - computeW(skillLine, 11) / 2, panelY + 69);
    }

    private void renderConfirmButton() {
        boolean hovered = false; // updated by mouse move

        Color bg     = Color.color(0.08, 0.18, 0.09);
        Color border = Color.color(0.15, 0.9, 0.4, 0.8 + bgPulse * 0.2);

        gc.setFill(bg);
        gc.fillRoundRect(BTN_X, BTN_Y, BTN_W, BTN_H, 10, 10);
        gc.setStroke(border);
        gc.setLineWidth(2);
        gc.strokeRoundRect(BTN_X, BTN_Y, BTN_W, BTN_H, 10, 10);

        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        gc.setFill(Color.color(0.15, 0.9, 0.4));
        String btnText = "CHOOSE WEAPON  ▶";
        gc.fillText(btnText, BTN_X + BTN_W / 2 - computeW(btnText, 18) / 2, BTN_Y + 33);
    }

    private void renderVeins() {
        gc.setStroke(Color.color(0.1, 0.5, 0.2, 0.04 + bgPulse * 0.03));
        gc.setLineWidth(1.2);
        double[][] veins = {
                {0, H*0.3, W*0.15, H*0.5, W*0.3, H*0.4},
                {W, H*0.6, W*0.85, H*0.7, W*0.7, H*0.65},
                {W*0.2, 0, W*0.25, H*0.2, W*0.18, H*0.4},
                {W*0.8, H, W*0.78, H*0.75, W*0.85, H*0.55}
        };
        for (double[] v : veins) {
            gc.beginPath();
            gc.moveTo(v[0], v[1]);
            gc.quadraticCurveTo(v[2], v[3], v[4], v[5]);
            gc.stroke();
        }
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

    private void proceedToWeaponSelect() {
        WeaponSelectScene ws = new WeaponSelectScene(selected);
        Main.setScene(ws.getScene());
    }

    private double computeW(String text, double size) {
        return text.length() * size * 0.52;
    }

    public Scene getScene() { return scene; }
}
