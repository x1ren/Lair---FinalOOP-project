package org.example.scenes;

import org.example.Main;
import javafx.animation.*;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.util.List;

/**
 * IntroScene — cinematic dialogue intro for THE LAIR.
 *
 * Phases:
 *   0  : Title card  "T H E   L A I R"
 *   1  : Setting     Campus at night, six friends, final project
 *   2  : Dialogue    Friends talking / arguing
 *   3  : The meteor  Sky lights up — crash
 *   4  : Caesar      "Just one picture..." — flash — infection
 *   5  : The attack  Friends knocked out
 *   6  : Awakening   Sir Khai speaks — something feels off
 *   7  : Transition  → Character Select
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
            new DialogueLine("—",       "Six students stayed behind to finish their final project: Joseph, Iben, Ilde, Gaille, Jamuel, and Caesar.", "calm"),
            new DialogueLine("JAMUEL",  "Sir Khai already told us what to fix. We just have to finish.", "calm"),
            new DialogueLine("IBEN",    "Something feels wrong outside.", "calm"),

            new DialogueLine("—",       "An hour later, the sky ignites.", "event"),
            new DialogueLine("—",       "A meteor tears across the darkness and crashes near the school grounds.", "event"),
            new DialogueLine("JOSEPH",  "Back away from the windows.", "shock"),
            new DialogueLine("ILDE",    "We should check that out.", "shock"),
            new DialogueLine("JOSEPH",  "No. Deadline first. We finish the project.", "shock"),
            new DialogueLine("—",       "They return to the classroom. Caesar stays behind.", "event"),

            new DialogueLine("CAESAR",  "Just one picture.", "caesar"),
            new DialogueLine("—",       "The camera flash goes off.", "event"),
            new DialogueLine("—",       "The meteor cracks open.", "horror"),
            new DialogueLine("—",       "A thick glowing gas erupts out and swallows Caesar whole.", "horror"),
            new DialogueLine("—",       "His veins light up. His bones twist. His eyes stop looking human.", "horror"),
            new DialogueLine("—",       "Then he attacks.", "horror"),
            new DialogueLine("—",       "The five survivors are thrown across desks and concrete. Darkness follows.", "horror"),

            new DialogueLine("—",       "When consciousness returns, the school no longer feels familiar.", "awaken"),
            new DialogueLine("—",       "Lights flicker red. Vein-like growths pulse through the walls.", "awaken"),
            new DialogueLine("SIR KHAI","Don't force yourself up yet. You're alive because your exposure was incomplete.", "khai"),
            new DialogueLine("SIR KHAI","The meteor carried LAIR. It is a living pathogen.", "khai"),
            new DialogueLine("SIR KHAI","Full exposure mutates the host. Partial exposure creates synchronization.", "khai"),
            new DialogueLine("SIR KHAI","The gas spread through the campus. Many students and staff are already infected.", "khai"),
            new DialogueLine("SIR KHAI","Caesar became the primary host. You five became something else.", "khai"),
            new DialogueLine("SIR KHAI","The infection amplified what was already inside you.", "khai"),
            new DialogueLine("SIR KHAI","Fight through the school. Reach Caesar. Bring back the stabilized vial.", "khai"),
            new DialogueLine("SIR KHAI","If we are lucky, it will be a cure.", "khai")
    );

    // ── State ─────────────────────────────────────────────────
    private int    lineIndex    = -1; // -1 = title card
    private boolean titlePhase  = true;
    private boolean finished    = false;

    // ── Animation ─────────────────────────────────────────────
    private String   displayText  = "";
    private int      charCount    = 0;
    private Timeline typewriter;

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

        // Typewriter effect
        int totalChars = displayText.length();
        double duration = Math.min(totalChars * 0.032, 3.0);

        typewriter = new Timeline(
                new KeyFrame(Duration.seconds(duration), e -> charCount = totalChars)
        );
        typewriter.setCycleCount(1);

        // Animate char count
        AnimationTimer typer = new AnimationTimer() {
            long start = 0;
            @Override public void handle(long now) {
                if (start == 0) start = now;
                double elapsed = (now - start) / 1_000_000_000.0;
                charCount = (int) Math.min(totalChars, elapsed / duration * totalChars);
                redraw();
                if (charCount >= totalChars) stop();
            }
        };
        typer.start();

        redraw();
    }

    // ── Render ────────────────────────────────────────────────

    private void renderTitle() {
        gc.setFill(Color.color(0.01, 0.02, 0.03));
        gc.fillRect(0, 0, W, H);

        gc.setFill(Color.color(0.02, 0.08, 0.10));
        for (int row = 0; row < 18; row++) {
            for (int col = 0; col < 32; col++) {
                if ((row + col) % 3 == 0) {
                    gc.fillRect(col * 40, row * 40, 40, 40);
                }
                if ((row + col) % 7 == 0) {
                    gc.setFill(Color.color(0.08, 0.28, 0.20, 0.3));
                    gc.fillRect(col * 40 + 10, row * 40 + 10, 12, 12);
                    gc.setFill(Color.color(0.02, 0.08, 0.10));
                }
            }
        }

        drawPixelPanel(156, 124, W - 312, 240, Color.color(0.01, 0.03, 0.05, 0.96),
                Color.color(0.10, 0.80, 0.42));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 72));
        gc.setFill(Color.color(0.15, 0.9, 0.45));
        String title = "T H E   L A I R";
        double tw = computeTextWidth(title, 86);
        gc.fillText(title, W / 2.0 - tw / 2, H / 2.0 - 26);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        gc.setFill(Color.color(0.7, 0.7, 0.7, 0.8));
        String sub = "The Beginning of the End";
        double sw = computeTextWidth(sub, 18);
        gc.fillText(sub.toUpperCase(), W / 2.0 - sw / 2, H / 2.0 + 26);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(Color.color(0.45, 0.8, 0.6));
        String prompt = "[ Click or press SPACE to begin ]";
        double pw = computeTextWidth(prompt, 13);
        gc.fillText(prompt.toUpperCase(), W / 2.0 - pw / 2, H - 64);
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
        double groundY = H * 0.62;

        gc.setFill(Color.color(0.05, 0.10, 0.07));
        gc.fillRect(0, groundY, W, H - groundY);
        gc.setFill(Color.color(0.08, 0.08, 0.12));
        gc.fillRect(W * 0.1, groundY - 200, W * 0.8, 200);

        Color windowColor = (phase.equals("horror") || phase.equals("awaken"))
                ? Color.color(0.8, 0.1, 0.05, 0.6 + bgPulse * 0.3)
                : Color.color(0.9, 0.85, 0.5, 0.3);
        gc.setFill(windowColor);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                gc.fillRect(W * 0.15 + col * 80, groundY - 180 + row * 55, 40, 28);
            }
        }

        if (phase.equals("calm") || phase.equals("shock") || phase.equals("caesar")) {
            gc.setFill(Color.color(0.05, 0.05, 0.08));
            double[] positions = {W*0.3, W*0.38, W*0.46, W*0.54, W*0.62, W*0.70};
            for (int i = 0; i < 6; i++) {
                drawSilhouette(gc, positions[i], groundY - 2, 14, 44);
            }
        }

        if (phase.equals("awaken") || phase.equals("khai")) {
            gc.setFill(Color.color(0.2, 0.8, 0.4, 0.5));
            drawSilhouette(gc, W * 0.5, groundY - 2, 16, 52);
        }

        if (phase.equals("horror") || phase.equals("caesar")) {
            gc.setFill(Color.color(0.15, 0.9, 0.3, 0.08 + bgPulse * 0.06));
            gc.fillRect(snap(W * 0.6), snap(groundY - 60), 140, 80);
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
        double boxH   = 160;
        double boxY   = H - boxH - 20;
        double boxX   = 40;
        double boxW   = W - 80;

        Color borderColor = switch (phase) {
            case "horror"  -> Color.color(0.9, 0.1, 0.1, 0.8);
            case "awaken"  -> Color.color(0.6, 0.2, 0.9, 0.8);
            case "khai"    -> Color.color(0.15, 0.9, 0.4, 0.9);
            case "caesar"  -> Color.color(0.9, 0.7, 0.1, 0.8);
            case "event"   -> Color.color(0.4, 0.4, 0.5, 0.6);
            default        -> Color.color(0.3, 0.4, 0.6, 0.7);
        };
        drawPixelPanel(boxX, boxY, boxW, boxH, Color.color(0.04, 0.04, 0.07, 0.94), borderColor);

        if (!line.speaker().equals("—")) {
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
            gc.setFill(borderColor);
            gc.fillText(line.speaker(), boxX + 20, boxY + 26);

            double nameW = computeTextWidth(line.speaker(), 15);
            gc.setFill(Color.color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 0.4));
            gc.fillRect(boxX + 20, boxY + 30, nameW, 2);
        }

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        gc.setFill(line.speaker().equals("—")
                ? Color.color(0.6, 0.6, 0.65)
                : Color.WHITE);

        String partial = displayText.substring(0, Math.min(charCount, displayText.length()));
        double textY   = line.speaker().equals("—") ? boxY + 50 : boxY + 58;
        wrapText(gc, partial, boxX + 22, textY, boxW - 44, 26);

        if (charCount >= displayText.length()) {
            double t = System.currentTimeMillis() / 600.0;
            gc.setFill(Color.color(0.5, 0.5, 0.5, 0.5 + 0.5 * Math.sin(t)));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
            gc.fillText("> CLICK OR SPACE", boxX + boxW - 190, boxY + boxH - 16);
        }
    }

    private void renderProgress() {
        int total = lines.size();
        double dotSpacing = 10;
        double totalW = total * dotSpacing;
        double startX = W / 2.0 - totalW / 2;
        double dotY = H - 10;

        for (int i = 0; i < total; i++) {
            boolean active = i <= lineIndex;
            gc.setFill(active
                    ? Color.color(0.15, 0.9, 0.4, 0.8)
                    : Color.color(0.3, 0.3, 0.3, 0.5));
            gc.fillRect(startX + i * dotSpacing, dotY - 4, 5, 5);
        }
    }

    // ── Transition ────────────────────────────────────────────

    private void transitionToCharSelect() {
        finished = true;
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
            CharacterSelectScene charSelect = new CharacterSelectScene();
            Main.setScene(charSelect.getScene());
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
