package org.example.ui;

import javafx.animation.*;
import javafx.scene.Scene;
import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.util.Duration;
import org.example.Main;
import org.example.app.GameContext;

import java.net.URL;
import java.util.List;

/**
 * EndingScene — cinematic ending cutscene with line-by-line dialogue.
 * 
 * Heavy, emotional, and regretful tone with controlled timing.
 * Each line appears with typewriter effect, followed by intentional pauses.
 * Includes subtle screen effects: vignette, slow fade-ins, and camera shake.
 */
public class EndingScene {

    private static final int W = Main.WIDTH;
    private static final int H = Main.HEIGHT;

    private final Scene scene;
    private final Canvas canvas = new Canvas(W, H);
    private final GraphicsContext gc = canvas.getGraphicsContext2D();

    // ── Dialogue data with timing ─────────────────────────────
    private record DialogueLine(String text, double pauseAfter, double typeSpeed) {}

    private final List<DialogueLine> lines = List.of(
            new DialogueLine("I can feel it…", 1.2, 0.045),
            new DialogueLine("It's still here.", 1.5, 0.045),
            new DialogueLine("No matter how many times I try to ignore it… it won't go away.", 2.0, 0.040),
            new DialogueLine("…it's inside me.", 1.8, 0.050),
            new DialogueLine("All those bullets… all that power…", 1.5, 0.042),
            new DialogueLine("I thought it was saving me.", 2.0, 0.045),
            new DialogueLine("But every fight… every shot…", 1.5, 0.042),
            new DialogueLine("I was getting closer to it.", 1.5, 0.045),
            new DialogueLine("Closer to becoming just like Hunos.", 2.2, 0.040),
            new DialogueLine("…I should've stopped him.", 2.0, 0.048),
            new DialogueLine("I should've stayed.", 1.5, 0.048),
            new DialogueLine("If I didn't let him go back there…", 1.8, 0.042),
            new DialogueLine("…maybe none of this would've happened.", 2.5, 0.040),
            new DialogueLine("We were just supposed to finish our project.", 1.5, 0.042),
            new DialogueLine("Complain about deadlines.", 1.2, 0.045),
            new DialogueLine("Go home like it was a normal night.", 2.0, 0.042),
            new DialogueLine("…I didn't even get to say goodbye.", 2.5, 0.045),
            new DialogueLine("You guys trusted me.", 1.5, 0.048),
            new DialogueLine("And I couldn't save any of you.", 2.3, 0.045),
            new DialogueLine("I don't deserve to walk away from this.", 2.0, 0.042),
            new DialogueLine("Not like this.", 1.8, 0.048),
            new DialogueLine("Not while this thing is still alive inside me.", 2.5, 0.038),
            new DialogueLine("…I'm sorry.", 2.0, 0.055),
            new DialogueLine("I'm really sorry.", 3.0, 0.050)
    );

    // ── State ─────────────────────────────────────────────────
    private int lineIndex = 0;
    private String displayText = "";
    private int charCount = 0;
    private boolean lineComplete = false;
    private boolean waitingForPause = false;
    private boolean showGoodbye = false;
    private boolean showTheEnd = false;
    private boolean finished = false;

    // ── Animation ─────────────────────────────────────────────
    private Timeline typewriter;
    private AnimationTimer typewriterTimer;
    private Timeline pauseTimer;
    private MediaPlayer bgmPlayer;

    // ── Visual effects ────────────────────────────────────────
    private double vignetteIntensity = 0.0;
    private double fadeAlpha = 1.0;
    private double shakeX = 0;
    private double shakeY = 0;
    private double purpleTint = 0.0;
    private double textShake = 0.0;
    private AnimationTimer effectsTimer;

    public EndingScene() {
        Pane root = new Pane(canvas);
        scene = new Scene(root, W, H);
        scene.setCursor(javafx.scene.Cursor.DEFAULT);

        startBackgroundMusic();
        startEffectsLoop();
        showNextLine();
    }

    // ── Background music ──────────────────────────────────────

    private void startBackgroundMusic() {
        URL url = EndingScene.class.getResource("/assets/ending/Ending.mp3");
        if (url == null) {
            System.err.println("ERROR: Could not find /assets/ending/Ending.mp3");
            return;
        }

        try {
            Media media = new Media(url.toExternalForm());
            bgmPlayer = new MediaPlayer(media);
            bgmPlayer.setVolume(0.35);
            bgmPlayer.play();
            System.out.println("Background music started: " + url);
        } catch (RuntimeException e) {
            System.err.println("ERROR: Failed to play background music: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopBackgroundMusic() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer.dispose();
            bgmPlayer = null;
        }
    }

    // ── Effects loop ──────────────────────────────────────────

    private void startEffectsLoop() {
        effectsTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateEffects();
                redraw();
            }
        };
        effectsTimer.start();
    }

    private void updateEffects() {
        // Gradually increase vignette
        if (vignetteIntensity < 0.75) {
            vignetteIntensity += 0.002;
        }

        // Fade in from black at start
        if (fadeAlpha > 0 && lineIndex < 3) {
            fadeAlpha -= 0.008;
        }

        // Purple tint when mentioning "inside me"
        if (lineIndex == 3 && charCount > 5) {
            purpleTint = Math.min(0.15, purpleTint + 0.003);
        } else if (purpleTint > 0) {
            purpleTint -= 0.002;
        }

        // Subtle camera shake during intense moments
        if (lineIndex >= 8 && lineIndex <= 12) {
            shakeX = (Math.random() - 0.5) * 1.5;
            shakeY = (Math.random() - 0.5) * 1.5;
        } else {
            shakeX *= 0.9;
            shakeY *= 0.9;
        }

        // Text shake for "goodbye"
        if (showGoodbye) {
            textShake = (Math.random() - 0.5) * 2.0;
        }
    }

    // ── Show next line ────────────────────────────────────────

    private void showNextLine() {
        if (lineIndex >= lines.size()) {
            showGunCockSequence();
            return;
        }

        DialogueLine line = lines.get(lineIndex);
        displayText = line.text();
        charCount = 0;
        lineComplete = false;
        waitingForPause = false;

        if (typewriter != null) typewriter.stop();
        if (typewriterTimer != null) typewriterTimer.stop();

        // Typewriter effect
        int totalChars = displayText.length();
        double duration = totalChars * line.typeSpeed();

        typewriter = new Timeline(
                new KeyFrame(Duration.seconds(duration), e -> {
                    charCount = totalChars;
                    lineComplete = true;
                    startPauseTimer(line.pauseAfter());
                })
        );
        typewriter.setCycleCount(1);

        // Animate char count
        typewriterTimer = new AnimationTimer() {
            long start = 0;
            @Override
            public void handle(long now) {
                if (start == 0) start = now;
                double elapsed = (now - start) / 1_000_000_000.0;
                charCount = (int) Math.min(totalChars, elapsed / duration * totalChars);
                if (charCount >= totalChars) {
                    stop();
                }
            }
        };
        typewriterTimer.start();
        typewriter.play();
    }

    private void startPauseTimer(double pauseDuration) {
        waitingForPause = true;
        pauseTimer = new Timeline(
                new KeyFrame(Duration.seconds(pauseDuration), e -> {
                    waitingForPause = false;
                    lineIndex++;
                    showNextLine();
                })
        );
        pauseTimer.setCycleCount(1);
        pauseTimer.play();
    }

    // ── Final sequence ────────────────────────────────────────

    private void showGunCockSequence() {
        // Skip directly to showing goodbye after a pause
        Timeline sequence = new Timeline(
                new KeyFrame(Duration.seconds(2.5), e -> {
                    showGoodbye = true;
                }),
                new KeyFrame(Duration.seconds(4.5), e -> {
                    showGoodbye = false;
                    cutToBlack();
                })
        );
        sequence.setCycleCount(1);
        sequence.play();
    }

    private void cutToBlack() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, W, H);

        Timeline sequence = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> {
                    // Play gunshot
                    AudioClip gunshot = GameContext.assets().audio("audio.ending.gunshot");
                    if (gunshot != null) {
                        gunshot.play(0.85);
                        System.out.println("Gunshot played");
                    } else {
                        System.err.println("ERROR: Gunshot audio not found in AssetRegistry");
                    }
                    stopBackgroundMusic();
                }),
                new KeyFrame(Duration.seconds(2.5), e -> {
                    showTheEnd = true;
                }),
                new KeyFrame(Duration.seconds(6.5), e -> {
                    cleanup();
                    // Return to main menu or exit
                    System.exit(0);
                })
        );
        sequence.setCycleCount(1);
        sequence.play();
    }

    // ── Render ────────────────────────────────────────────────

    private void redraw() {
        if (finished) return;

        gc.save();
        gc.translate(shakeX, shakeY);

        // Background - dim, desaturated
        Color bgColor = Color.color(0.02, 0.02, 0.03);
        gc.setFill(bgColor);
        gc.fillRect(0, 0, W, H);

        // Purple tint overlay
        if (purpleTint > 0) {
            gc.setFill(Color.color(0.4, 0.1, 0.5, purpleTint));
            gc.fillRect(0, 0, W, H);
        }

        // Vignette effect
        RadialGradient vignette = new RadialGradient(
                0, 0, W / 2.0, H / 2.0, Math.max(W, H) * 0.6,
                false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(0, 0, 0, 0)),
                new Stop(1, Color.color(0, 0, 0, vignetteIntensity))
        );
        gc.setFill(vignette);
        gc.fillRect(0, 0, W, H);

        // Render current state
        if (showTheEnd) {
            renderTheEnd();
        } else if (showGoodbye) {
            renderGoodbye();
        } else {
            renderDialogue();
        }

        // Fade overlay
        if (fadeAlpha > 0) {
            gc.setFill(Color.color(0, 0, 0, fadeAlpha));
            gc.fillRect(0, 0, W, H);
        }

        gc.restore();
    }

    private void renderDialogue() {
        if (lineIndex >= lines.size()) return;

        String partial = displayText.substring(0, Math.min(charCount, displayText.length()));

        gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 18));
        gc.setFill(Color.color(0.85, 0.85, 0.88, 0.95));

        double textX = W / 2.0;
        double textY = H / 2.0;

        // Center text
        double textWidth = computeTextWidth(partial, 18);
        gc.fillText(partial, textX - textWidth / 2, textY);

        // Subtle line fade effect
        if (lineComplete && waitingForPause) {
            double fadeOut = Math.min(0.15, (System.currentTimeMillis() % 1000) / 5000.0);
            gc.setFill(Color.color(0, 0, 0, fadeOut));
            gc.fillRect(0, textY - 30, W, 60);
        }
    }

    private void renderGoodbye() {
        gc.setFont(Font.font("Monospaced", FontWeight.NORMAL, 20));
        gc.setFill(Color.color(0.88, 0.88, 0.90, 0.98));
        String text = "…goodbye.";
        double textWidth = computeTextWidth(text, 20);
        gc.fillText(text, W / 2.0 - textWidth / 2 + textShake, H / 2.0);
    }

    private void renderTheEnd() {
        // Fade in "THE END"
        double alpha = Math.min(1.0, (System.currentTimeMillis() % 10000) / 2000.0);
        
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 48));
        gc.setFill(Color.color(0.9, 0.9, 0.92, alpha));
        String text = "THE END";
        double textWidth = computeTextWidth(text, 48);
        gc.fillText(text, W / 2.0 - textWidth / 2, H / 2.0);
    }

    // ── Utilities ─────────────────────────────────────────────

    private double computeTextWidth(String text, double fontSize) {
        javafx.scene.text.Text temp = new javafx.scene.text.Text(text);
        temp.setFont(Font.font("Monospaced", fontSize));
        return temp.getLayoutBounds().getWidth();
    }

    private void cleanup() {
        finished = true;
        if (typewriter != null) typewriter.stop();
        if (typewriterTimer != null) typewriterTimer.stop();
        if (pauseTimer != null) pauseTimer.stop();
        if (effectsTimer != null) effectsTimer.stop();
        stopBackgroundMusic();
    }

    public Scene getScene() {
        return scene;
    }
}
