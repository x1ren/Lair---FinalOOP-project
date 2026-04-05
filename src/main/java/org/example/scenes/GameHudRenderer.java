package org.example.scenes;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.gameplay.StageDefinition;
import org.example.player.CharacterType;
import org.example.weapons.Weapon;

final class GameHudRenderer {

    private final GraphicsContext gc;
    private final double viewportWidth;
    private final double viewportHeight;
    private final double pixel;

    GameHudRenderer(GraphicsContext gc, double viewportWidth, double viewportHeight, double pixel) {
        this.gc = gc;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.pixel = pixel;
    }

    void renderHud(StageDefinition stage, CharacterType character, Weapon weapon,
                   int hp, int maxHp, int ammo, double abilityFill,
                   String abilityStatus, String reloadStatus, String buffText,
                   boolean exitOpen) {
        double panelY = 18;
        double accentR = stage.tint().getRed();
        double accentG = stage.tint().getGreen();
        double accentB = stage.tint().getBlue();

        drawPixelPanel(20, panelY, 320, 104, Color.color(0.01, 0.03, 0.05, 0.84),
                Color.color(0.10, 0.76, 0.42, 0.72));
        drawPixelPanel(viewportWidth / 2.0 - 190, panelY, 380, 52, Color.color(0.01, 0.03, 0.05, 0.78),
                Color.color(accentR, accentG, accentB, 0.70));
        drawPixelPanel(viewportWidth - 224, panelY, 204, 104, Color.color(0.01, 0.03, 0.05, 0.84),
                Color.color(0.10, 0.76, 0.42, 0.72));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 18));
        gc.setFill(Color.WHITE);
        gc.fillText(character.getName(), 36, panelY + 28);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
        gc.setFill(Color.color(0.74, 0.86, 0.80));
        gc.fillText(character.getTitle() + " | " + weapon.getName(), 36, panelY + 48);

        gc.setFill(Color.WHITE);
        gc.fillText("HP " + hp + "/" + maxHp, 36, panelY + 68);
        drawBar(36, panelY + 74, 288, 10, hp / (double) maxHp, Color.color(0.88, 0.2, 0.2));

        gc.setFill(Color.WHITE);
        gc.fillText("SKILL " + abilityStatus, 36, panelY + 96);
        drawBar(36, panelY + 102, 288, 10, abilityFill, Color.color(0.22, 0.7, 0.92));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.color(0.74, 0.86, 0.80));
        gc.fillText(stage.name().toUpperCase(), viewportWidth / 2.0 - textWidth(stage.name(), 12) / 2, panelY + 20);

        gc.setFont(Font.font("Monospaced", 11));
        gc.setFill(Color.WHITE);
        String objective = stage.objective().toUpperCase();
        gc.fillText(objective, viewportWidth / 2.0 - textWidth(objective, 11) / 2, panelY + 38);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
        gc.setFill(Color.WHITE);
        gc.fillText("AMMO", viewportWidth - 204, panelY + 28);
        gc.fillText(ammo + "/" + weapon.getMagazineSize(), viewportWidth - 204, panelY + 48);
        gc.fillText("RELOAD", viewportWidth - 204, panelY + 72);
        gc.fillText(reloadStatus, viewportWidth - 204, panelY + 92);

        if (buffText != null) {
            drawPixelPanel(20, viewportHeight - 74, 260, 38, Color.color(0.01, 0.03, 0.05, 0.82),
                    Color.color(0.10, 0.76, 0.42, 0.70));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
            gc.setFill(Color.color(0.15, 0.9, 0.4));
            gc.fillText(buffText, 34, viewportHeight - 50);
        }

        if (exitOpen) {
            drawPixelPanel(viewportWidth - 220, viewportHeight - 74, 200, 38, Color.color(0.01, 0.03, 0.05, 0.82),
                    Color.color(0.10, 0.76, 0.42, 0.70));
            gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 11));
            gc.setFill(Color.color(0.15, 0.9, 0.4));
            gc.fillText("EXIT OPEN  MOVE INTO MARKER", viewportWidth - 204, viewportHeight - 50);
        }
    }

    void renderStageIntro(StageDefinition stage) {
        gc.setFill(Color.color(0, 0, 0, 0.54));
        gc.fillRect(0, 0, viewportWidth, viewportHeight);

        drawPixelPanel(180, 210, viewportWidth - 360, 190, Color.color(0.03, 0.04, 0.06, 0.96),
                Color.color(stage.tint().getRed(), stage.tint().getGreen(), stage.tint().getBlue(), 0.95));

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 24));
        gc.setFill(Color.WHITE);
        gc.fillText(stage.name(), viewportWidth / 2.0 - textWidth(stage.name(), 24) / 2, 265);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        gc.setFill(Color.color(0.7, 0.75, 0.72));
        gc.fillText(stage.objective().toUpperCase(), viewportWidth / 2.0 - textWidth(stage.objective(), 15) / 2, 304);

        gc.setFont(Font.font("Monospaced", 13));
        gc.setFill(Color.color(0.86, 0.86, 0.86));
        wrapText(stage.description(), 220, 338, viewportWidth - 440, 20);
    }

    void renderStatusBanner(String statusText) {
        drawPixelPanel(viewportWidth / 2.0 - 180, viewportHeight - 94, 360, 42, Color.color(0.02, 0.03, 0.04, 0.92),
                Color.color(0.18, 0.8, 0.32, 0.85));
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 16));
        gc.setFill(Color.color(0.9, 0.95, 0.9));
        gc.fillText(statusText, viewportWidth / 2.0 - textWidth(statusText, 16) / 2, viewportHeight - 66);
    }

    void renderEndOverlay(boolean victory) {
        gc.setFill(Color.color(0, 0, 0, 0.76));
        gc.fillRect(0, 0, viewportWidth, viewportHeight);

        drawPixelPanel(120, 120, viewportWidth - 240, viewportHeight - 240, Color.color(0.04, 0.05, 0.06, 0.98),
                Color.color(0.18, 0.82, 0.34, 0.85));

        String title = victory ? "THE LAIR" : "Synchronization Failed";
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 34));
        gc.setFill(Color.WHITE);
        gc.fillText(title, viewportWidth / 2.0 - textWidth(title, 34) / 2, 190);

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 15));
        gc.setFill(Color.color(0.75, 0.8, 0.76));
        String subtitle = victory
                ? "Caesar was only the first host. The real trap was waiting in the courtyard."
                : "The campus falls silent as LAIR keeps learning.";
        gc.fillText(subtitle, viewportWidth / 2.0 - textWidth(subtitle, 15) / 2, 230);

        gc.setFont(Font.font("Monospaced", 13));
        gc.setFill(Color.color(0.9, 0.9, 0.9));
        if (victory) {
            wrapText("Caesar falls in the gym and drops a stabilized LAIR vial, but the real Sir Khai was already dead. "
                            + "The creature guiding you was LAIR wearing his face, learning trust before it fed. "
                            + "In the courtyard, the mimic reveals itself and the final fight ends the night's worst lie.",
                    170, 290, viewportWidth - 340, 28);
        } else {
            wrapText("You were close, but the synchronized aura failed before the school could be reclaimed. "
                            + "Caesar remains the first host, and the false Sir Khai keeps feeding the campus to LAIR.",
                    170, 300, viewportWidth - 340, 28);
        }

        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        gc.setFill(Color.color(0.18, 0.85, 0.32));
        String prompt = "Press ENTER or SPACE to return to character select";
        gc.fillText(prompt, viewportWidth / 2.0 - textWidth(prompt, 14) / 2, viewportHeight - 150);
    }

    private void drawBar(double x, double y, double width, double height, double fill, Color color) {
        x = snap(x);
        y = snap(y);
        width = snap(width);
        height = snap(height);

        gc.setFill(Color.color(0.12, 0.14, 0.16));
        gc.fillRect(x, y, width, height);
        gc.setFill(Color.color(0.05, 0.06, 0.08));
        gc.fillRect(x + pixel, y + pixel, width - pixel * 2, height - pixel * 2);
        gc.setFill(color);
        gc.fillRect(x + pixel, y + pixel, (width - pixel * 2) * clamp(fill, 0, 1), height - pixel * 2);
    }

    private void drawPixelPanel(double x, double y, double width, double height, Color bg, Color border) {
        x = snap(x);
        y = snap(y);
        width = snap(width);
        height = snap(height);

        gc.setFill(border);
        gc.fillRect(x, y, width, height);
        gc.setFill(Color.color(0.02, 0.03, 0.03));
        gc.fillRect(x + pixel, y + pixel, width - pixel * 2, height - pixel * 2);
        gc.setFill(bg);
        gc.fillRect(x + pixel * 2, y + pixel * 2, width - pixel * 4, height - pixel * 4);
    }

    private void wrapText(String text, double x, double y, double maxWidth, double lineHeight) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        double currentY = y;

        for (String word : words) {
            String nextLine = line.isEmpty() ? word : line + " " + word;
            if (textWidth(nextLine, 16) > maxWidth) {
                gc.fillText(line.toString(), x, currentY);
                currentY += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(nextLine);
            }
        }

        if (!line.isEmpty()) {
            gc.fillText(line.toString(), x, currentY);
        }
    }

    private double snap(double value) {
        return Math.round(value / pixel) * pixel;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double textWidth(String text, double size) {
        return text.length() * size * 0.52;
    }
}
