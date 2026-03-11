package org.example.scenes;

import org.example.Main;
import org.example.player.CharacterType;
import org.example.weapons.*;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.text.*;

import java.util.List;

/**
 * Weapon Selection Screen for THE LAIR.
 *
 * POLYMORPHISM in action:
 *   All 5 weapons are stored as List<Weapon> (base type).
 *   drawIcon(), getProjectileColor(), getType() are called
 *   polymorphically — each weapon renders itself differently
 *   without this scene knowing the concrete type.
 *
 * Displays:
 *   - Weapon cards with icon (drawn by each weapon's drawIcon())
 *   - Full stat breakdown
 *   - Final player stats AFTER weapon modifiers applied
 */
public class WeaponSelectScene {

    private static final int W = Main.WIDTH;
    private static final int H = Main.HEIGHT;

    private final Scene scene;
    private final Canvas canvas = new Canvas(W, H);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    private final CharacterType character;

    // POLYMORPHISM: stored as base type Weapon
    private final List<Weapon> weapons = WeaponFactory.getAllWeapons();
    private Weapon selected;
    private int hoveredIndex = -1;

    // Card layout
    private static final double CARD_W     = 190;
    private static final double CARD_H     = 260;
    private static final double CARD_GAP   = 18;
    private static final double CARDS_Y    = 150;
    private static final double CARDS_START_X =
            (W - (5 * CARD_W + 4 * CARD_GAP)) / 2.0;

    // Confirm button
    private static final double BTN_W = 260;
    private static final double BTN_H = 52;
    private static final double BTN_X = W / 2.0 - BTN_W / 2;
    private static final double BTN_Y = H - 85;

    private double bgPulse  = 0;
    private double animTime = 0;

    public WeaponSelectScene(CharacterType character) {
        this.character = character;
        this.selected  = weapons.get(0); // default: AR

        Pane root = new Pane(canvas);
        scene = new Scene(root, W, H);
        scene.setCursor(javafx.scene.Cursor.DEFAULT);

        scene.setOnMouseMoved(e -> hoveredIndex = getCardAt(e.getX(), e.getY()));

        scene.setOnMouseClicked(e -> {
            int idx = getCardAt(e.getX(), e.getY());
            if (idx >= 0 && idx < weapons.size()) {
                selected = weapons.get(idx);
            }

            if (e.getX() >= BTN_X && e.getX() <= BTN_X + BTN_W
                    && e.getY() >= BTN_Y && e.getY() <= BTN_Y + BTN_H) {
                launchGame();
            }

            // Back button
            if (e.getX() >= 30 && e.getX() <= 130 && e.getY() >= H - 46 && e.getY() <= H - 20) {
                Main.setScene(new CharacterSelectScene().getScene());
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
                bgPulse  = (Math.sin(t * 2.0) + 1) / 2.0;
                animTime = t;
                render();
            }
        };
        loop.start();
    }

    private void render() {
        // ── Background ────────────────────────────────────────
        gc.setFill(Color.color(0.03, 0.03, 0.04));
        gc.fillRect(0, 0, W, H);

        renderVeins();

        // ── Header ────────────────────────────────────────────
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.15, 0.9, 0.4, 0.5));
        String lairTag = "— T H E   L A I R —";
        gc.fillText(lairTag, W / 2.0 - cw(lairTag, 11) / 2, 48);

        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        gc.setFill(Color.WHITE);
        String heading = "Choose Your Weapon";
        gc.fillText(heading, W / 2.0 - cw(heading, 36) / 2, 94);

        // Character reminder
        gc.setFont(Font.font("Georgia", FontPosture.ITALIC, 13));
        gc.setFill(Color.color(0.15, 0.9, 0.4, 0.7));
        String charLine = character.name + "  ·  " + character.title;
        gc.fillText(charLine, W / 2.0 - cw(charLine, 13) / 2, 120);

        // ── Weapon cards ──────────────────────────────────────
        for (int i = 0; i < weapons.size(); i++) {
            double cx = CARDS_START_X + i * (CARD_W + CARD_GAP);
            boolean isSel = weapons.get(i) == selected;
            boolean isHov = i == hoveredIndex;
            renderWeaponCard(weapons.get(i), cx, CARDS_Y, isSel, isHov);
        }

        // ── Detail panel for selected weapon ─────────────────
        renderDetailPanel();

        // ── Confirm button ────────────────────────────────────
        renderConfirmButton();

        // ── Back button ───────────────────────────────────────
        gc.setFont(Font.font("Georgia", 13));
        gc.setFill(Color.color(0.45, 0.45, 0.5));
        gc.fillText("← Back", 38, H - 25);
    }

    // ── Weapon card ───────────────────────────────────────────

    private void renderWeaponCard(Weapon w, double x, double y,
                                  boolean sel, boolean hov) {
        double lift  = sel ? -10 : (hov ? -4 : 0);
        double cardY = y + lift;

        // Glow
        if (sel) {
            Color gc2 = w.getProjectileColor(); // POLYMORPHISM
            gc.setFill(Color.color(gc2.getRed(), gc2.getGreen(), gc2.getBlue(), 0.15));
            gc.fillRoundRect(x - 8, cardY + 6, CARD_W + 16, CARD_H + 16, 14, 14);
        }

        // Background
        gc.setFill(sel
                ? Color.color(0.07, 0.07, 0.10, 0.95)
                : Color.color(0.055, 0.055, 0.075, 0.85));
        gc.fillRoundRect(x, cardY, CARD_W, CARD_H, 10, 10);

        // Border — uses weapon's own projectile color (POLYMORPHISM)
        Color projColor = w.getProjectileColor();
        gc.setStroke(sel
                ? projColor
                : Color.color(0.22, 0.22, 0.28, 0.55));
        gc.setLineWidth(sel ? 2.0 : 1.0);
        gc.strokeRoundRect(x, cardY, CARD_W, CARD_H, 10, 10);

        // ── Weapon icon area ──────────────────────────────────
        double iconAreaH = 90;
        gc.setFill(Color.color(0.06, 0.06, 0.09));
        gc.fillRoundRect(x + 8, cardY + 8, CARD_W - 16, iconAreaH, 6, 6);

        // POLYMORPHISM: each weapon draws its own icon
        w.drawIcon(gc, x + 12, cardY + 12, CARD_W - 24, iconAreaH - 8);

        // ── Weapon name ───────────────────────────────────────
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        gc.setFill(sel ? projColor : Color.color(0.85, 0.85, 0.85));
        String name = w.getName();
        gc.fillText(name, x + CARD_W / 2 - cw(name, 14) / 2, cardY + 116);

        // ── Type badge ────────────────────────────────────────
        gc.setFont(Font.font("Courier New", 9));
        gc.setFill(Color.color(projColor.getRed(), projColor.getGreen(),
                projColor.getBlue(), 0.7));
        String type = w.getType().name().replace("_", " ");
        gc.fillText(type, x + CARD_W / 2 - cw(type, 9) / 2, cardY + 132);

        // ── Mini stat bars ────────────────────────────────────
        double statY = cardY + 145;
        renderWeaponStatBar(gc, "DMG",    x + 10, statY,      CARD_W - 20, normalizeDmg(w.getDamage()), sel, projColor);
        renderWeaponStatBar(gc, "RATE",   x + 10, statY + 18, CARD_W - 20, normalizeFireRate(w.getFireRate()), sel, projColor);
        renderWeaponStatBar(gc, "MAG",    x + 10, statY + 36, CARD_W - 20, normalizeMag(w.getMagazineSize()), sel, projColor);
        renderWeaponStatBar(gc, "SPEED",  x + 10, statY + 54, CARD_W - 20, w.getMoveSpeedModifier() / 1.1, sel, projColor);

        // ── Speed modifier hint ───────────────────────────────
        gc.setFont(Font.font("Courier New", 9));
        double spd = (w.getMoveSpeedModifier() - 1.0) * 100;
        Color spdColor = spd >= 0 ? Color.color(0.3, 0.9, 0.4) : Color.color(0.9, 0.3, 0.3);
        gc.setFill(spdColor);
        String spdStr = String.format("Move %s%.0f%%", spd >= 0 ? "+" : "", spd);
        gc.fillText(spdStr, x + CARD_W / 2 - cw(spdStr, 9) / 2, cardY + CARD_H - 24);

        // ── Selected badge ────────────────────────────────────
        if (sel) {
            gc.setFill(projColor);
            gc.fillRoundRect(x + CARD_W / 2 - 35, cardY + CARD_H - 16, 70, 14, 5, 5);
            gc.setFont(Font.font("Courier New", FontWeight.BOLD, 9));
            gc.setFill(Color.BLACK);
            gc.fillText("EQUIPPED", x + CARD_W / 2 - 24, cardY + CARD_H - 5);
        }
    }

    private void renderWeaponStatBar(GraphicsContext gc, String label,
                                     double x, double y, double maxW,
                                     double fill, boolean active, Color color) {
        gc.setFont(Font.font("Courier New", 9));
        gc.setFill(Color.color(0.4, 0.4, 0.45));
        gc.fillText(label, x, y + 9);

        double barX = x + 36;
        double barW = maxW - 40;
        fill = Math.max(0, Math.min(1, fill));

        gc.setFill(Color.color(0.1, 0.1, 0.12));
        gc.fillRoundRect(barX, y, barW, 9, 3, 3);

        gc.setFill(active
                ? Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.85)
                : Color.color(0.3, 0.4, 0.35, 0.6));
        gc.fillRoundRect(barX, y, barW * fill, 9, 3, 3);
    }

    // ── Detail panel ──────────────────────────────────────────

    private void renderDetailPanel() {
        double panelY = CARDS_Y + CARD_H + 16;
        double panelH = 70;

        gc.setFill(Color.color(0.04, 0.06, 0.04, 0.88));
        gc.fillRoundRect(40, panelY, W - 80, panelH, 10, 10);
        gc.setStroke(Color.color(0.15, 0.55, 0.25, 0.35));
        gc.setLineWidth(1);
        gc.strokeRoundRect(40, panelY, W - 80, panelH, 10, 10);

        // Weapon description
        gc.setFont(Font.font("Georgia", FontPosture.ITALIC, 13));
        gc.setFill(Color.color(0.75, 0.78, 0.74));
        String[] descLines = selected.getDescription().split("\n");
        for (int i = 0; i < descLines.length; i++) {
            gc.fillText(descLines[i],
                    W / 2.0 - cw(descLines[i], 13) / 2,
                    panelY + 20 + i * 18);
        }

        // Final computed stats
        double finalSpeed = character.getFinalSpeed(selected);
        double finalJump  = Math.abs(character.getFinalJump(selected));

        gc.setFont(Font.font("Courier New", 11));
        gc.setFill(Color.color(0.15, 0.9, 0.4, 0.8));
        String statsLine = String.format(
                "HP: %d   |   Logic: %d   |   Mana: %d   |   Speed: %.0f px/s   |   Jump: %.0f   |   Weapon DMG: %d",
                character.getHealth(),
                character.getLogic(),
                character.getMana(),
                finalSpeed,
                finalJump,
                selected.getDamage()
        );
        gc.fillText(statsLine, W / 2.0 - cw(statsLine, 11) / 2, panelY + panelH - 10);
    }

    private void renderConfirmButton() {
        Color proj = selected.getProjectileColor();
        double pulse = 0.7 + bgPulse * 0.3;

        gc.setFill(Color.color(proj.getRed() * 0.3, proj.getGreen() * 0.3,
                proj.getBlue() * 0.3, 0.9));
        gc.fillRoundRect(BTN_X, BTN_Y, BTN_W, BTN_H, 10, 10);

        gc.setStroke(Color.color(proj.getRed(), proj.getGreen(),
                proj.getBlue(), pulse));
        gc.setLineWidth(2);
        gc.strokeRoundRect(BTN_X, BTN_Y, BTN_W, BTN_H, 10, 10);

        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        gc.setFill(Color.color(proj.getRed(), proj.getGreen(),
                proj.getBlue(), 0.95));
        String btn = "ENTER THE LAIR  ▶";
        gc.fillText(btn, BTN_X + BTN_W / 2 - cw(btn, 18) / 2, BTN_Y + 33);
    }

    private void renderVeins() {
        gc.setStroke(Color.color(0.08, 0.45, 0.18, 0.035 + bgPulse * 0.025));
        gc.setLineWidth(1.0);
        double[][] veins = {
                {0, H*0.25, W*0.12, H*0.45, W*0.25, H*0.38},
                {W, H*0.55, W*0.88, H*0.65, W*0.72, H*0.60},
                {W*0.15, 0, W*0.22, H*0.18, W*0.14, H*0.38},
                {W*0.82, H, W*0.80, H*0.72, W*0.87, H*0.52}
        };
        for (double[] v : veins) {
            gc.beginPath(); gc.moveTo(v[0], v[1]);
            gc.quadraticCurveTo(v[2], v[3], v[4], v[5]);
            gc.stroke();
        }
    }

    // ── Normalizers for stat bars (0–1) ───────────────────────

    private double normalizeDmg(int dmg) {
        // max dmg = sniper 90, min = smg 12
        return (dmg - 12.0) / (90.0 - 12.0);
    }

    private double normalizeFireRate(double fr) {
        // lower fireRate = faster; invert so bar = speed of fire
        // range: 0.06 (smg) to 1.8 (sniper)
        return 1.0 - ((fr - 0.06) / (1.8 - 0.06));
    }

    private double normalizeMag(int mag) {
        return (mag - 5.0) / (100.0 - 5.0);
    }

    private int getCardAt(double mx, double my) {
        for (int i = 0; i < weapons.size(); i++) {
            double cx = CARDS_START_X + i * (CARD_W + CARD_GAP);
            if (mx >= cx && mx <= cx + CARD_W
                    && my >= CARDS_Y - 12 && my <= CARDS_Y + CARD_H + 8) {
                return i;
            }
        }
        return -1;
    }

    private void launchGame() {
        GameScene game = new GameScene(character, selected);
        Main.setScene(game.getScene());
    }

    private double cw(String text, double size) {
        return text.length() * size * 0.52;
    }

    public Scene getScene() { return scene; }
}
