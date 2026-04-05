package org.example.scenes;

import org.example.Main;
import javafx.animation.*;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import org.example.runtime.GameContext;

import java.util.List;

/**
 * IntroScene — cinematic dialogue intro for THE LAIR.
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
        double frameX = 84;
        double frameY = 92;
        double frameW = W - 168;
        double frameH = 332;
        double groundY = frameY + frameH - 74;
        Color accent = getPhaseAccent(phase);

        drawPixelPanel(frameX, frameY, frameW, frameH, Color.color(0.02, 0.05, 0.07, 0.96), accent);
        gc.setFill(Color.color(0.03, 0.08, 0.10, 0.95));
        gc.fillRect(snap(frameX + 12), snap(frameY + 12), frameW - 24, 28);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.color(0.76, 0.86, 0.82));
        String phaseLabel = getPhaseLabel(phase);
        gc.fillText(phaseLabel, frameX + 24, frameY + 31);

        String locationLabel = getLocationLabel(phase);
        gc.setFill(Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.95));
        gc.fillText(locationLabel, frameX + frameW - computeTextWidth(locationLabel, 12) - 24, frameY + 31);

        gc.setFill(Color.color(0.05, 0.12, 0.14));
        gc.fillRect(snap(frameX + 16), snap(frameY + 52), frameW - 32, frameH - 68);
        gc.setFill(Color.color(0.03, 0.08, 0.09));
        gc.fillRect(snap(frameX + 16), snap(groundY), frameW - 32, frameY + frameH - groundY - 16);

        renderEnvironmentBlocks(frameX + 16, frameY + 52, frameW - 32, groundY - (frameY + 52), phase);
        renderSceneActors(phase, speaker, groundY, frameX + 16, frameW - 32);

        gc.setFill(Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.18));
        gc.fillRect(snap(frameX + 20), snap(frameY + frameH - 28), 120, 10);
        gc.setFill(Color.color(0.76, 0.86, 0.82));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 10));
        gc.fillText("SCENE FRAME ACTIVE", frameX + 24, frameY + frameH - 18);
    }

    private void renderEnvironmentBlocks(double x, double y, double width, double height, String phase) {
        if (phase.equals("calm") || phase.equals("shock")) {
            gc.setFill(Color.color(0.08, 0.09, 0.13));
            gc.fillRect(snap(x + 64), snap(y + 28), width - 128, height - 50);

            Color windowColor = phase.equals("shock")
                    ? Color.color(1.0, 0.52, 0.12, 0.55 + bgPulse * 0.2)
                    : Color.color(0.82, 0.78, 0.44, 0.26);
            gc.setFill(windowColor);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 7; col++) {
                    gc.fillRect(snap(x + 100 + col * 96), snap(y + 48 + row * 56), 44, 28);
                }
            }

            gc.setFill(Color.color(0.12, 0.16, 0.20));
            gc.fillRect(snap(x + 90), snap(y + height - 62), width - 180, 14);
        }

        if (phase.equals("event") || phase.equals("caesar") || phase.equals("horror")) {
            gc.setFill(Color.color(0.04, 0.08, 0.06));
            gc.fillRect(snap(x + 40), snap(y + 34), width - 80, height - 38);
            gc.setFill(Color.color(0.09, 0.15, 0.11));
            gc.fillRect(snap(x + width * 0.14), snap(y + height - 82), width * 0.72, 12);

            gc.setFill(Color.color(0.74, 0.56, 0.16, phase.equals("event") ? 0.85 : 0.35));
            gc.fillRect(snap(x + width * 0.58), snap(y + 40), 32, 32);
            gc.fillRect(snap(x + width * 0.58 + 12), snap(y + 28), 8, 8);

            if (!phase.equals("event")) {
                gc.setFill(Color.color(0.12, 0.95, 0.38, 0.10 + bgPulse * 0.14));
                gc.fillRect(snap(x + width * 0.48), snap(y + 74), 168, 104);
            }
        }

        if (phase.equals("awaken") || phase.equals("khai")) {
            gc.setFill(Color.color(0.08, 0.04, 0.09));
            gc.fillRect(snap(x + 44), snap(y + 22), width - 88, height - 22);
            gc.setFill(Color.color(0.40, 0.05, 0.08, 0.34 + bgPulse * 0.1));
            for (int row = 0; row < 5; row++) {
                gc.fillRect(snap(x + 72 + row * 140), snap(y + 30 + row * 12), 12, height - 80 - row * 10);
            }
            gc.setFill(Color.color(0.76, 0.10, 0.10, 0.5 + bgPulse * 0.24));
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 7; col++) {
                    gc.fillRect(snap(x + 90 + col * 94), snap(y + 40 + row * 52), 34, 24);
                }
            }
        }
    }

    private void renderSceneActors(String phase, String speaker, double groundY, double x, double width) {
        if (phase.equals("calm") || phase.equals("shock") || phase.equals("caesar")) {
            gc.setFill(Color.color(0.06, 0.08, 0.10));
            double[] positions = {x + width * 0.26, x + width * 0.34, x + width * 0.42, x + width * 0.50, x + width * 0.58, x + width * 0.66};
            for (double position : positions) {
                drawSilhouette(gc, position, groundY - 2, 14, 44);
            }
        }

        if (phase.equals("event")) {
            gc.setFill(Color.color(0.06, 0.08, 0.10));
            double[] positions = {x + width * 0.32, x + width * 0.40, x + width * 0.48, x + width * 0.56, x + width * 0.64};
            for (double position : positions) {
                drawSilhouette(gc, position, groundY - 2, 14, 44);
            }
            gc.setFill(Color.color(0.78, 0.74, 0.76));
            drawSilhouette(gc, x + width * 0.74, groundY - 2, 14, 44);
        }

        if (phase.equals("horror")) {
            gc.setFill(Color.color(0.10, 0.92, 0.34, 0.8));
            drawSilhouette(gc, x + width * 0.68, groundY - 2, 18, 52);
            gc.setFill(Color.color(0.08, 0.08, 0.10, 0.8));
            drawSilhouette(gc, x + width * 0.34, groundY + 8, 14, 34);
            drawSilhouette(gc, x + width * 0.44, groundY + 10, 14, 32);
            drawSilhouette(gc, x + width * 0.54, groundY + 9, 14, 34);
        }

        if (phase.equals("awaken") || phase.equals("khai")) {
            gc.setFill(Color.color(0.8, 0.84, 0.88));
            drawSilhouette(gc, x + width * 0.42, groundY - 2, 16, 50);
            gc.setFill(Color.color(0.14, 0.9, 0.4, 0.62));
            drawSilhouette(gc, x + width * 0.60, groundY - 2, 16, 56);
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

    private void renderDialogueBox(DialogueLine line, String phase) {
        double boxH   = 172;
        double boxY   = H - boxH - 24;
        double boxX   = 84;
        double boxW   = W - 168;
        double metaW  = 172;
        Color accent = getPhaseAccent(phase);

        drawPixelPanel(boxX, boxY, boxW, boxH, Color.color(0.03, 0.04, 0.06, 0.97), accent);
        drawPixelPanel(boxX + 14, boxY + 14, metaW, boxH - 28, Color.color(0.04, 0.07, 0.08, 0.95), Color.color(0.14, 0.18, 0.20));
        drawPixelPanel(boxX + metaW + 24, boxY + 14, boxW - metaW - 38, boxH - 28, Color.color(0.04, 0.05, 0.08, 0.95), Color.color(0.14, 0.18, 0.20));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(accent);
        gc.fillText("TRANSMISSION", boxX + 30, boxY + 38);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        gc.setFill(line.speaker().equals("—") ? Color.color(0.78, 0.80, 0.84) : Color.WHITE);
        gc.fillText(line.speaker().equals("—") ? "SYSTEM LOG" : line.speaker(), boxX + 30, boxY + 68);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.58, 0.68, 0.70));
        gc.fillText(getPhaseLabel(phase), boxX + 30, boxY + 96);
        gc.fillText(getLocationLabel(phase), boxX + 30, boxY + 118);
        gc.fillText(String.format("ENTRY %02d / %02d", lineIndex + 1, lines.size()), boxX + 30, boxY + 140);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        gc.setFill(line.speaker().equals("—")
                ? Color.color(0.74, 0.76, 0.80)
                : Color.WHITE);

        String partial = displayText.substring(0, Math.min(charCount, displayText.length()));
        double textX = boxX + metaW + 40;
        double textY = boxY + 44;
        wrapText(gc, partial, textX, textY, boxW - metaW - 70, 28);

        if (charCount >= displayText.length()) {
            double t = System.currentTimeMillis() / 600.0;
            gc.setFill(Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.45 + 0.45 * Math.sin(t)));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
            gc.fillText("> CLICK OR SPACE TO CONTINUE", boxX + boxW - 286, boxY + boxH - 18);
        }
    }

    private void renderProgress() {
        int total = lines.size();
        double trackW = W - 168;
        double startX = 84;
        double dotY = 74;
        double segmentW = trackW / total;

        for (int i = 0; i < total; i++) {
            boolean active = i <= lineIndex;
            gc.setFill(active
                    ? Color.color(0.15, 0.9, 0.4, 0.85)
                    : Color.color(0.16, 0.18, 0.20, 0.7));
            gc.fillRect(snap(startX + i * segmentW), dotY, Math.max(8, snap(segmentW - 6)), 8);
        }
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
                    String p = lines.get(lineIndex).phase();
                    if (p.equals("horror") || p.equals("awaken") || p.equals("khai")) {
                        redraw();
                    }
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
