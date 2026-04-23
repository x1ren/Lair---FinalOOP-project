package org.example.ui;

import org.example.Main;
import javafx.animation.*;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import org.example.assets.SpriteSheet;
import org.example.app.GameContext;

import java.util.List;

/**
 * IntroScene — cinematic dialogue intro for THE LAIR.
 *
 * Story order matches the run: calm classroom setup → meteor on the grounds → Caesar's photo and gas release →
 * the five survivors wake synced to LAIR while Caesar becomes primary host → Sir Khai sends you through the
 * school (Library, Canteen, Gym where Caesar falls, then Courtyard for the mimic). After this scene the player
 * picks which survivor they play and enters that same progression.
 *
 * Phases:
 *   0  : Title card
 *   1  : Setting     Sir Khai helps the group with their project
 *   2  : Meteor      The six friends investigate the crash
 *   3  : Infection   Caesar takes the photo and the gas spreads
 *   4  : Awakening   The school is changed and Sir Khai explains LAIR
 *   5  : Transition  Player chooses who awakened first
 */
public class IntroScene {

    /**
     * Per-frame non-transparent bounds inside each Hunos sheet cell (see {@link #drawCaesarIntroActor}).
     * The art is a tiny ~17×26px figure in a 110×120 / 120×120 cell; drawing the full cell at ~64px made Caesar
     * look miniature next to 32×32 sprites scaled to ~72px.
     */
    private static final int[][] CAESAR_IDLE_CELL_BBOX = {
            {52, 62, 17, 26}, {62, 63, 17, 25}, {72, 63, 17, 25}, {82, 62, 17, 26},
            {92, 62, 17, 26}, {102, 63, 8, 25}, {0, 63, 9, 25}, {2, 63, 17, 25},
            {12, 62, 17, 26}, {22, 63, 17, 25}, {32, 63, 17, 25}, {42, 62, 17, 26},
    };
    private static final int[][] CAESAR_WALK_CELL_BBOX = {
            {52, 62, 17, 26}, {52, 63, 17, 25}, {52, 63, 17, 25}, {52, 62, 17, 26},
    };

    // ── Layout ────────────────────────────────────────────────
    private static final int W = Main.WIDTH;
    private static final int H = Main.HEIGHT;
    private static final double PIXEL = 4;

    private final Scene scene;
    private final Canvas canvas = new Canvas(W, H);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    // ── Dialogue data ─────────────────────────────────────────
    private record DialogueLine(String speaker, String text, String phase) {}

    private final List<DialogueLine> lines = List.of(
            new DialogueLine("—",       "It was supposed to be an ordinary late night on campus.", "calm"),
            new DialogueLine("—",       "Before anything strange happened, the six friends asked Sir Khai for help with their final project.", "calm"),
            new DialogueLine("SIR KHAI","Joseph, Iben, Ilde, Gaille, Jamuel, Caesar. Fix the logic first, then the polish.", "calm"),
            new DialogueLine("—",       "They thanked him and returned to their classroom, unaware the night was about to change.", "calm"),

            new DialogueLine("—",       "About an hour later, the sky suddenly lit up.", "event"),
            new DialogueLine("—",       "A meteor tore through the darkness and crashed into the school grounds.", "event"),
            new DialogueLine("ILDE",    "We need to see that up close.", "shock"),
            new DialogueLine("—",       "The six of them rushed outside and found the meteor pulsing in the field.", "shock"),
            new DialogueLine("—",       "The others started heading back, but Caesar stayed behind for one quick picture.", "event"),

            new DialogueLine("CAESAR",  "Just one picture.", "caesar"),
            new DialogueLine("—",       "The flash went off. The meteor cracked open.", "horror"),
            new DialogueLine("—",       "A thick glowing gas burst out and swallowed Caesar first.", "horror"),
            new DialogueLine("—",       "The others rushed back to help him, but the mist spread around all six of them.", "horror"),
            new DialogueLine("—",       "Their bodies weakened. Their vision blurred. One by one they collapsed.", "horror"),

            new DialogueLine("—",       "When consciousness returned, the school was covered in red light and vein-like growths.", "awaken"),
            new DialogueLine("SIR KHAI","Easy now. The meteor carried LAIR, an extraterrestrial pathogen that spreads through gas.", "khai"),
            new DialogueLine("SIR KHAI","Most hosts lose control and become infected. The five of you synchronized with it instead.", "khai"),
            new DialogueLine("SIR KHAI","That aura in your body can manifest a weapon shaped by instinct and fighting style.", "khai"),
            new DialogueLine("SIR KHAI","Joseph's rifle bleeds targets. Jamuel's sniper punishes with precision. Gaille's shotgun erupts in bursts.", "khai"),
            new DialogueLine("SIR KHAI","Ilde moves fastest with an SMG. Iben carries a heavy LMG built for suppression.", "khai"),
            new DialogueLine("SIR KHAI","Caesar had full compatibility. He became the primary host and moved toward the gym.", "khai"),
            new DialogueLine("SIR KHAI","Fight through the library, canteen, and gym. Then return with the vial if Caesar drops it.", "khai"),
            new DialogueLine("SIR KHAI","My memory's kind of blurry. What was your name again?", "khai")
    );

    // ── State ─────────────────────────────────────────────────
    private int    lineIndex    = -1; // -1 = title card
    private boolean titlePhase  = true;
    private boolean finished    = false;

    // ── Animation ─────────────────────────────────────────────
    private String   displayText  = "";
    private int      charCount    = 0;
    private Timeline typewriter;
    private AnimationTimer typewriterTimer;

    // ── Background pulse (for horror segments) ────────────────
    private double bgPulse = 0;
    private AnimationTimer pulseTimer;

    public IntroScene() {
        Pane root = new Pane(canvas);
        scene = new Scene(root, W, H);
        scene.setCursor(javafx.scene.Cursor.DEFAULT);

        // Click anywhere to advance
        scene.setOnMouseClicked(e -> advance());
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case SPACE, ENTER, ESCAPE -> advance();
            }
        });

        startPulseLoop();
        renderTitle();
    }

    // ── Advance ───────────────────────────────────────────────

    private void advance() {
        if (finished) return;

        if (titlePhase) {
            titlePhase = false;
            lineIndex = 0;
            showLine(lineIndex);
            return;
        }

        // If typewriter still running — skip to full text
        if (typewriter != null && typewriter.getStatus() == Animation.Status.RUNNING) {
            typewriter.stop();
            charCount = displayText.length();
            redraw();
            return;
        }

        lineIndex++;
        if (lineIndex >= lines.size()) {
            transitionToCharSelect();
        } else {
            showLine(lineIndex);
        }
    }

    // ── Show a dialogue line ──────────────────────────────────

    private void showLine(int idx) {
        DialogueLine line = lines.get(idx);
        displayText = line.text();
        charCount = 0;

        if (typewriter != null) typewriter.stop();
        if (typewriterTimer != null) typewriterTimer.stop();

        // Typewriter effect
        int totalChars = displayText.length();
        double duration = Math.min(totalChars * 0.032, 3.0);

        typewriter = new Timeline(
                new KeyFrame(Duration.seconds(duration), e -> charCount = totalChars)
        );
        typewriter.setCycleCount(1);

        // Animate char count
        typewriterTimer = new AnimationTimer() {
            long start = 0;
            @Override public void handle(long now) {
                if (start == 0) start = now;
                double elapsed = (now - start) / 1_000_000_000.0;
                charCount = (int) Math.min(totalChars, elapsed / duration * totalChars);
                redraw();
                if (charCount >= totalChars) stop();
            }
        };
        typewriterTimer.start();

        redraw();
    }

    // ── Render ────────────────────────────────────────────────

    private void renderTitle() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, W, H);

        gc.setFill(Color.color(0.2, 0.8, 0.4, 0.15));
        for (int i = 0; i < 60; i++) {
            double px = (i * 317 + 80) % W;
            double py = (i * 211 + 50) % H;
            gc.fillOval(px, py, 2, 2);
        }

        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 86));
        gc.setFill(Color.color(0.15, 0.9, 0.45));
        String title = "T H E   L A I R";
        double tw = computeTextWidth(title, 86);
        gc.fillText(title, W / 2.0 - tw / 2, H / 2.0 - 20);

        gc.setFill(Color.color(0.15, 0.9, 0.45, 0.25));
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 90));
        gc.fillText(title, W / 2.0 - tw / 2 - 2, H / 2.0 - 18);

        gc.setFont(Font.font("Georgia", FontPosture.ITALIC, 18));
        gc.setFill(Color.color(0.7, 0.7, 0.7, 0.8));
        String sub = "The Beginning of the End";
        double sw = computeTextWidth(sub, 18);
        gc.fillText(sub, W / 2.0 - sw / 2, H / 2.0 + 40);

        gc.setFont(Font.font("Courier New", 13));
        gc.setFill(Color.color(0.4, 0.4, 0.4));
        String prompt = "[ Click or press SPACE to begin ]";
        double pw = computeTextWidth(prompt, 13);
        gc.fillText(prompt, W / 2.0 - pw / 2, H - 50);

        Image preview = GameContext.assets().image("character.preview");
        if (preview != null) {
            gc.setImageSmoothing(false);
            gc.drawImage(preview, W / 2.0 - 24, H / 2.0 + 66, 48, 48);
        }

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(GameContext.assets().isPreloadComplete()
                ? Color.color(0.18, 0.9, 0.42)
                : Color.color(0.88, 0.82, 0.20));
        String preload = GameContext.assets().isPreloadComplete() ? "ASSET SYNC COMPLETE" : "SYNCING SPRITES + AUDIO";
        gc.fillText(preload, W / 2.0 - computeTextWidth(preload, 12) / 2, H - 82);
    }

    private void redraw() {
        if (titlePhase) { renderTitle(); return; }
        if (lineIndex >= lines.size()) return;

        DialogueLine line = lines.get(lineIndex);
        String phase = line.phase();

        // ── Background ────────────────────────────────────────
        Color bgColor = switch (phase) {
            case "horror"  -> Color.color(0.06 + bgPulse * 0.04, 0.0, 0.0);
            case "awaken"  -> Color.color(0.04, 0.0, 0.06 + bgPulse * 0.03);
            case "khai"    -> Color.color(0.02, 0.04, 0.02);
            case "caesar"  -> Color.color(0.05, 0.04, 0.0);
            case "shock"   -> Color.color(0.05, 0.05, 0.07);
            case "event"   -> Color.color(0.03, 0.03, 0.05);
            default        -> Color.color(0.06, 0.06, 0.10);
        };
        gc.setFill(bgColor);
        gc.fillRect(0, 0, W, H);

        // ── Atmospheric particles ─────────────────────────────
        renderAtmosphere(phase);

        // ── Scene illustration area ───────────────────────────
        renderSceneIllustration(phase, line.speaker());

        // ── Dialogue box ──────────────────────────────────────
        renderDialogueBox(line, phase);

        // ── Progress indicator ────────────────────────────────
        renderProgress();
    }

    private void renderAtmosphere(String phase) {
        if (phase.equals("horror") || phase.equals("awaken") || phase.equals("khai")) {
            gc.setFill(Color.color(0.10, 0.84, 0.34, 0.10 + bgPulse * 0.08));
            for (int i = 0; i < 8; i++) {
                double sx = (i * 180 + 40) % W;
                gc.fillRect(sx, 0, 4, H);
                double sy = (i * 130 + 20) % H;
                gc.fillRect(0, sy, W, 4);
            }
        }

        if (phase.equals("calm") || phase.equals("shock")) {
            gc.setFill(Color.color(0.8, 0.9, 1.0, 0.55));
            for (int i = 0; i < 40; i++) {
                double px = (i * 397 + 50) % W;
                double py = (i * 213 + 20) % (H / 2);
                gc.fillRect(snap(px), snap(py), 2, 2);
            }
        }

        if (phase.equals("shock") && lineIndex >= 8) {
            gc.setFill(Color.color(1.0, 0.6, 0.1, 0.7));
            for (int i = 0; i < 18; i++) {
                gc.fillRect(W * 0.8 - i * 14, i * 12, 12, 4);
            }
            gc.setFill(Color.color(1.0, 0.8, 0.3, 0.4));
            gc.fillRect(snap(W * 0.53), snap(H * 0.32), 24, 24);
        }
    }

    private void renderSceneIllustration(String phase, String speaker) {
        double frameX = 80;
        double frameY = 74;
        double frameW = W - 160;
        double frameH = 374;
        double groundY = frameY + frameH - 74;
        Color accent = getPhaseAccent(phase);

        drawPixelPanel(frameX, frameY, frameW, frameH, Color.color(0.02, 0.05, 0.07, 0.90), Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.55));

        double sceneX = frameX + 16;
        double sceneY = frameY + 16;
        double sceneW = frameW - 32;
        double sceneH = frameH - 32;

        renderEnvironmentBlocks(sceneX, sceneY, sceneW, sceneH, phase);
        renderSceneActors(phase, speaker, groundY, sceneX, sceneW);

        gc.setFill(Color.color(0.01, 0.02, 0.03, 0.28));
        gc.fillRect(snap(sceneX), snap(sceneY), sceneW, 28);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.84, 0.88, 0.90, 0.92));
        gc.fillText(getPhaseLabel(phase), sceneX + 14, sceneY + 19);

        String locationLabel = getLocationLabel(phase);
        gc.setFill(Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.94));
        gc.fillText(locationLabel, sceneX + sceneW - computeTextWidth(locationLabel, 11) - 14, sceneY + 19);
    }

    private void renderEnvironmentBlocks(double x, double y, double width, double height, String phase) {
        Image backdrop = switch (phase) {
            case "calm", "shock", "awaken", "khai" -> GameContext.assets().image("stage.library");
            case "event", "caesar", "horror" -> GameContext.assets().image("stage.courtyard");
            default -> null;
        };

        if (backdrop != null) {
            gc.setImageSmoothing(false);
            gc.drawImage(backdrop, snap(x), snap(y), width, height);
        } else {
            gc.setFill(Color.color(0.05, 0.12, 0.14));
            gc.fillRect(snap(x), snap(y), width, height);
        }

        gc.setFill(switch (phase) {
            case "calm" -> Color.color(0.06, 0.08, 0.12, 0.44);
            case "shock" -> Color.color(0.20, 0.10, 0.04, 0.36 + bgPulse * 0.18);
            case "event" -> Color.color(0.03, 0.08, 0.10, 0.48);
            case "caesar" -> Color.color(0.12, 0.08, 0.02, 0.46);
            case "horror" -> Color.color(0.08 + bgPulse * 0.03, 0.00, 0.00, 0.58);
            case "awaken" -> Color.color(0.18, 0.02, 0.05, 0.50);
            case "khai" -> Color.color(0.02, 0.10, 0.04, 0.56);
            default -> Color.color(0.00, 0.00, 0.00, 0.38);
        });
        gc.fillRect(snap(x), snap(y), width, height);

        gc.setFill(Color.color(0.03, 0.08, 0.09, 0.88));
        gc.fillRect(snap(x), snap(y + height - 74), width, 74);

        if (phase.equals("shock") || phase.equals("event") || phase.equals("caesar") || phase.equals("horror")) {
            double meteorX = x + width * 0.68;
            double meteorY = y + height * 0.26;
            gc.setFill(Color.color(1.0, 0.64, 0.18, phase.equals("event") ? 0.70 : 0.48));
            gc.fillOval(snap(meteorX - 36), snap(meteorY - 28), 52, 52);
            gc.setFill(Color.color(1.0, 0.90, 0.30, 0.45));
            gc.fillRect(snap(meteorX - 12), snap(meteorY - 8), 20, 20);
        }

        if (phase.equals("horror") || phase.equals("awaken") || phase.equals("khai")) {
            gc.setStroke(Color.color(0.12, 0.92, 0.36, 0.28 + bgPulse * 0.18));
            gc.setLineWidth(4);
            for (int i = 0; i < 6; i++) {
                double startX = x + 80 + i * 150;
                gc.strokeLine(startX, y + 18, startX + 32, y + height - 70);
                gc.strokeLine(startX + 24, y + 82, startX - 12, y + 132);
            }
        }
    }

    private void renderSceneActors(String phase, String speaker, double groundY, double x, double width) {
        String[] survivors = {
                "character.joseph",
                "character.iben",
                "character.ilde",
                "character.gaille",
                "character.jamuel"
        };

        if (phase.equals("calm") || phase.equals("shock")) {
            drawSirKhaiActor(x + width * 0.14, groundY, 72, 0, 
                    animatedFrame(8, 4.0, 0), speaker.equals("SIR KHAI"));
            for (int i = 0; i < survivors.length; i++) {
                drawCharacterActor(survivors[i], x + width * (0.28 + i * 0.09), groundY, 72,
                        0, animatedFrame(8, 4.0, i * 0.17), false, 1.0);
            }
            drawCaesarIntroActor(x + width * 0.78, groundY, 84, speaker.equals("CAESAR"), phase);
        }

        if (phase.equals("event")) {
            for (int i = 0; i < survivors.length; i++) {
                drawCharacterActor(survivors[i], x + width * (0.24 + i * 0.08), groundY, 70,
                        1, animatedFrame(4, 6.0, i * 0.13), false, 0.98);
            }
            drawCaesarIntroActor(x + width * 0.72, groundY, 82, false, phase);
        }

        if (phase.equals("caesar")) {
            for (int i = 0; i < 2; i++) {
                drawCharacterActor(survivors[i], x + width * (0.26 + i * 0.10), groundY - 8, 58,
                        1, animatedFrame(4, 5.0, i * 0.12), false, 0.48);
            }
            drawCaesarIntroActor(x + width * 0.68, groundY, 88, speaker.equals("CAESAR"), phase);
        }

        if (phase.equals("horror")) {
            for (int i = 0; i < survivors.length; i++) {
                drawCharacterActor(survivors[i], x + width * (0.26 + i * 0.08), groundY + 8, 66,
                        4, Math.min(5, 1 + i), false, 0.82);
            }
            drawCaesarIntroActor(x + width * 0.70, groundY - 6, 84, true, phase);
        }

        if (phase.equals("awaken") || phase.equals("khai")) {
            for (int i = 0; i < survivors.length; i++) {
                double centerX = x + width * (0.22 + i * 0.085);
                gc.setFill(Color.color(0.12, 0.90, 0.36, 0.12 + bgPulse * 0.10));
                gc.fillOval(centerX - 20, groundY - 58, 40, 56);
                drawCharacterActor(survivors[i], centerX, groundY, 70,
                        0, animatedFrame(8, 4.0, i * 0.21), false, 1.0);
            }
            double khaiX = x + width * 0.72;
            // LAIR briefing: Sir Khai is still the real teacher — human row 0 only (zombie row is for the mimic in gameplay).
            drawSirKhaiActor(khaiX, groundY, 72, 0,
                    animatedFrame(8, 4.0, 0.5), speaker.equals("SIR KHAI"));
        }

        if (!speaker.equals("—")) {
            double tagX = x + width - 170;
            double tagY = groundY - 158;
            drawPixelPanel(tagX, tagY, 138, 38, Color.color(0.03, 0.06, 0.05, 0.92), getPhaseAccent(phase));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
            gc.setFill(Color.WHITE);
            gc.fillText(speaker, tagX + 14, tagY + 24);
        }
    }

    private void drawSilhouette(GraphicsContext gc, double x, double y,
                                double w, double h) {
        double px = 4;
        double sx = snap(x - w / 2);
        double sy = snap(y - h);
        gc.fillRect(sx + px, sy, px * 3, px * 3);
        gc.fillRect(sx, sy + px * 3, px * 5, px * 8);
        gc.fillRect(sx + px, sy + px * 11, px, px * 3);
        gc.fillRect(sx + px * 3, sy + px * 11, px, px * 3);
    }

    private void drawCharacterActor(String assetId, double centerX, double groundY, double size,
                                    int row, int column, boolean flipX, double alpha) {
        SpriteSheet sheet = GameContext.assets().sheet(assetId, 32, 32);
        if (sheet == null) {
            gc.save();
            gc.setGlobalAlpha(alpha);
            gc.setFill(Color.color(0.82, 0.84, 0.88));
            drawSilhouette(gc, centerX, groundY, 16, 48);
            gc.restore();
            return;
        }

        gc.save();
        gc.setGlobalAlpha(alpha);
        sheet.drawFrame(gc, row, column, snap(centerX - size / 2), snap(groundY - size), size, size, flipX);
        gc.restore();
    }

    private void drawSirKhaiActor(double centerX, double groundY, double size,
                                  int row, int column, boolean highlighted) {
        SpriteSheet sheet = GameContext.assets().sheet("character.sir_khai", 32, 32);
        if (sheet == null) {
            gc.save();
            gc.setFill(Color.color(0.82, 0.86, 0.92));
            drawSilhouette(gc, centerX, groundY, 18, 54);
            gc.restore();
            return;
        }

        if (highlighted) {
            gc.save();
            gc.setFill(Color.color(0.82, 0.90, 0.82, 0.26));
            gc.fillRect(snap(centerX - 24), snap(groundY - size - 10), 48, size + 18);
            gc.restore();
        }

        gc.save();
        sheet.drawFrame(gc, row, column, snap(centerX - size / 2), snap(groundY - size), size, size, false);
        gc.restore();
    }

    /**
     * Human Caesar before LAIR takes him — {@code Hunos_Idle} / {@code Hunos_Walk}, not the infected gym boss sheet.
     * Idle+blink for calm / shock / horror; walk strip when he is out on the field (event / photo beat).
     *
     * @param targetHeight on-screen height for the cropped figure (sheet cells are mostly empty padding).
     */
    private void drawCaesarIntroActor(double centerX, double groundY, double targetHeight, boolean highlighted, String phase) {
        boolean walking = phase.equals("event") || phase.equals("caesar");
        String assetId = walking ? "intro.caesar_human_walk" : "intro.caesar_human_idle";
        int cellW = walking ? 120 : 110;
        int cellH = 120;
        int frameCount = walking ? 4 : 12;
        double fps = walking ? 5.0 : 3.2;

        Image img = GameContext.assets().image(assetId);
        if (img == null) {
            drawNamedFallbackActor("CAESAR", centerX, groundY, 22, 72,
                    Color.color(0.88, 0.72, 0.40), highlighted);
            return;
        }

        int col = animatedFrame(frameCount, fps, 0);
        int[][] boxes = walking ? CAESAR_WALK_CELL_BBOX : CAESAR_IDLE_CELL_BBOX;
        int[] box = boxes[Math.min(col, boxes.length - 1)];
        int margin = 4;
        double cellX = col * (double) cellW;
        double sx = cellX + box[0] - margin;
        double sy = box[1] - margin;
        double sw = box[2] + 2.0 * margin;
        double sh = box[3] + 2.0 * margin;
        double cellRight = cellX + cellW;
        if (sx < cellX) {
            sx = cellX;
        }
        if (sy < 0) {
            sy = 0;
        }
        if (sx + sw > cellRight) {
            sw = cellRight - sx;
        }
        if (sy + sh > cellH) {
            sh = cellH - sy;
        }
        if (sw < 1 || sh < 1) {
            return;
        }

        double destH = Math.max(56, targetHeight);
        double destW = destH * (sw / sh);
        double dx = snap(centerX - destW / 2);
        double dy = snap(groundY - destH);

        if (highlighted) {
            gc.save();
            gc.setFill(Color.color(0.92, 0.78, 0.22, 0.28));
            gc.fillRect(snap(centerX - destW / 2 - 4), snap(groundY - destH - 12), destW + 8, destH + 20);
            gc.restore();
        }
        gc.save();
        gc.setImageSmoothing(false);
        gc.drawImage(img, sx, sy, sw, sh, dx, dy, destW, destH);
        gc.restore();
    }

    private void drawNamedFallbackActor(String label, double centerX, double groundY, double width,
                                        double height, Color color, boolean highlighted) {
        gc.save();
        gc.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), highlighted ? 0.26 : 0.14));
        gc.fillRect(snap(centerX - 24), snap(groundY - height - 10), 48, height + 18);
        gc.setFill(color);
        drawSilhouette(gc, centerX, groundY, width, height);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 9));
        gc.setFill(Color.WHITE);
        gc.fillText(label, centerX - computeTextWidth(label, 9) / 2, groundY - height - 16);
        gc.restore();
    }

    private int animatedFrame(int frameCount, double fps, double offset) {
        if (frameCount <= 1) {
            return 0;
        }
        double t = System.currentTimeMillis() / 1000.0;
        return (int) Math.floor((t + offset) * fps) % frameCount;
    }

    private void renderDialogueBox(DialogueLine line, String phase) {
        double boxH   = 144;
        double boxY   = H - boxH - 28;
        double boxX   = 80;
        double boxW   = W - 160;
        Color accent = getPhaseAccent(phase);

        drawPixelPanel(boxX, boxY, boxW, boxH, Color.color(0.02, 0.03, 0.05, 0.95), Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.45));

        if (!line.speaker().equals("—")) {
            drawPixelPanel(boxX + 20, boxY + 16, 148, 32, Color.color(0.04, 0.06, 0.08, 0.96), Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.88));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 13));
            gc.setFill(Color.WHITE);
            gc.fillText(line.speaker(), boxX + 34, boxY + 37);
        }

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.60, 0.70, 0.74));
        String entry = String.format("%02d / %02d", lineIndex + 1, lines.size());
        gc.fillText(entry, boxX + boxW - computeTextWidth(entry, 11) - 24, boxY + 28);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 17));
        gc.setFill(line.speaker().equals("—") ? Color.color(0.82, 0.84, 0.88) : Color.WHITE);
        String partial = displayText.substring(0, Math.min(charCount, displayText.length()));
        double textX = boxX + 26;
        double textY = line.speaker().equals("—") ? boxY + 42 : boxY + 68;
        wrapText(gc, partial, textX, textY, boxW - 52, 28);

        if (charCount >= displayText.length()) {
            double t = System.currentTimeMillis() / 600.0;
            gc.setFill(Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.42 + 0.36 * Math.sin(t)));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
            String prompt = "click or press space";
            gc.fillText(prompt, boxX + boxW - computeTextWidth(prompt, 12) - 24, boxY + boxH - 18);
        }
    }

    private void renderProgress() {
        // Entry count in the dialogue box replaces the old debug-like progress strip.
    }

    private Color getPhaseAccent(String phase) {
        return switch (phase) {
            case "horror" -> Color.color(0.92, 0.18, 0.16, 0.92);
            case "awaken" -> Color.color(0.62, 0.34, 0.90, 0.88);
            case "khai" -> Color.color(0.15, 0.9, 0.4, 0.92);
            case "caesar" -> Color.color(0.92, 0.70, 0.16, 0.88);
            case "event" -> Color.color(0.52, 0.62, 0.72, 0.75);
            case "shock" -> Color.color(0.22, 0.72, 0.92, 0.80);
            default -> Color.color(0.26, 0.58, 0.76, 0.78);
        };
    }

    private String getPhaseLabel(String phase) {
        return switch (phase) {
            case "calm" -> "PRE-INCIDENT";
            case "shock" -> "IMPACT WITNESS";
            case "event" -> "FIELD APPROACH";
            case "caesar" -> "FLASH CONTACT";
            case "horror" -> "INFECTION EVENT";
            case "awaken" -> "POST-EXPOSURE";
            case "khai" -> "LAIR BRIEFING";
            default -> "NARRATIVE";
        };
    }

    private String getLocationLabel(String phase) {
        return switch (phase) {
            case "calm", "shock" -> "CLASSROOM BLOCK";
            case "event", "caesar", "horror" -> "SCHOOL GROUNDS";
            case "awaken", "khai" -> "INFECTED HALLWAY";
            default -> "CAMPUS";
        };
    }

    // ── Transition ────────────────────────────────────────────

    private void transitionToCharSelect() {
        finished = true;
        if (typewriter != null) typewriter.stop();
        if (typewriterTimer != null) typewriterTimer.stop();
        if (pulseTimer != null) pulseTimer.stop();

        // Fade to black then switch
        Timeline fade = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {}),
                new KeyFrame(Duration.seconds(1.2), e -> {
                    gc.setFill(Color.BLACK);
                    gc.fillRect(0, 0, W, H);
                })
        );
        fade.setOnFinished(e -> {
            GameContext.showCharacterSelect();
        });
        fade.play();
    }

    // ── Background pulse loop ─────────────────────────────────

    private void startPulseLoop() {
        pulseTimer = new AnimationTimer() {
            long start = 0;
            @Override public void handle(long now) {
                if (start == 0) start = now;
                double t = (now - start) / 1_000_000_000.0;
                bgPulse = (Math.sin(t * 2.5) + 1) / 2.0;
                if (!titlePhase && lineIndex >= 0 && lineIndex < lines.size()) {
                    redraw();
                }
            }
        };
        pulseTimer.start();
    }

    // ── Helpers ───────────────────────────────────────────────

    private void wrapText(GraphicsContext gc, String text, double x, double y,
                          double maxWidth, double lineHeight) {
        if (text == null || text.isEmpty()) return;
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        double currentY = y;

        for (String word : words) {
            String test = line.isEmpty() ? word : line + " " + word;
            if (computeTextWidth(test, 17) > maxWidth) {
                gc.fillText(line.toString(), x, currentY);
                currentY += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (!line.isEmpty()) gc.fillText(line.toString(), x, currentY);
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

    private double snap(double value) {
        return Math.round(value / PIXEL) * PIXEL;
    }

    private double computeTextWidth(String text, double fontSize) {
        // Approximate: ~0.52 * fontSize per character
        return text.length() * fontSize * 0.52;
    }

    public Scene getScene() { return scene; }
}
