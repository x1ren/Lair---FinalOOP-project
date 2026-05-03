package org.example.app;

import javafx.application.Application;
import org.example.player.CharacterType;

import java.util.List;

/**
 * Optional CLI shortcuts for faster iteration. Parsed from {@link Application#getParameters()} unnamed args,
 * {@link String[]} passed to {@code main}, and/or system property {@code lair.stage}.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code mvn javafx:run -Dlair.cmd.args="--stage 3"}</li>
 *   <li>{@code mvn javafx:run -Dlair.stage=3}</li>
 *   <li>{@code java ... org.example.Main --stage 3}</li>
 * </ul>
 * Stage numbers are <strong>1-based</strong> (1 = first story stage). Username is forced to {@code debug}.
 */
public final class LaunchConfig {

    public static final String DEBUG_PLAYER_NAME = "debug";

    private static Integer debugStage1Based;
    private static CharacterType debugCharacter = CharacterType.JOSEPH_JIMENEZ;

    private LaunchConfig() {
    }

    /** Call from {@code main} before {@link Application#launch(String...)} so early props are visible. */
    public static void bootstrapFromMain(String[] args) {
        parseTokens(List.of(args != null ? args : new String[0]));
        applySystemPropertyStageIfUnset();
    }

    /** Call from {@link Application#start}; merges JavaFX unnamed parameters. */
    public static void parseApplicationParameters(Application.Parameters parameters) {
        if (parameters != null) {
            parseTokens(parameters.getUnnamed());
        }
        applySystemPropertyStageIfUnset();
    }

    private static void applySystemPropertyStageIfUnset() {
        if (debugStage1Based != null) {
            return;
        }
        String prop = System.getProperty("lair.stage");
        if (prop == null || prop.isBlank()) {
            return;
        }
        try {
            debugStage1Based = Integer.parseInt(prop.trim());
        } catch (NumberFormatException ignored) {
            System.err.println("[LaunchConfig] Ignoring invalid lair.stage=" + prop);
        }
    }

    private static void parseTokens(List<String> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            String a = tokens.get(i);
            if ("--stage".equals(a) && i + 1 < tokens.size()) {
                try {
                    debugStage1Based = Integer.parseInt(tokens.get(++i).trim());
                } catch (NumberFormatException ex) {
                    System.err.println("[LaunchConfig] Expected integer after --stage");
                }
            } else if (a.startsWith("--stage=")) {
                try {
                    debugStage1Based = Integer.parseInt(a.substring("--stage=".length()).trim());
                } catch (NumberFormatException ex) {
                    System.err.println("[LaunchConfig] Invalid --stage= value");
                }
            } else if ("--debug-character".equals(a) && i + 1 < tokens.size()) {
                try {
                    debugCharacter = CharacterType.valueOf(tokens.get(++i).trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    System.err.println("[LaunchConfig] Unknown character; using default.");
                }
            } else if (a.startsWith("--debug-character=")) {
                try {
                    debugCharacter = CharacterType.valueOf(a.substring("--debug-character=".length()).trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    System.err.println("[LaunchConfig] Unknown character; using default.");
                }
            }
        }
    }

    public static boolean isDebugStageLaunch() {
        return debugStage1Based != null;
    }

    public static String debugStage1BasedDisplay() {
        return debugStage1Based == null ? "-" : String.valueOf(debugStage1Based);
    }

    public static String debugPlayerName() {
        return DEBUG_PLAYER_NAME;
    }

    public static CharacterType debugCharacter() {
        return debugCharacter;
    }

    /** Converts 1-based stage from CLI to 0-based index, clamped to {@code [0, stageCount - 1]}. */
    public static int debugStageIndex0Based(int stageCount) {
        if (stageCount <= 0) {
            return 0;
        }
        if (debugStage1Based == null) {
            return 0;
        }
        int s = debugStage1Based;
        if (s < 1) {
            s = 1;
        }
        if (s > stageCount) {
            s = stageCount;
        }
        return s - 1;
    }
}
