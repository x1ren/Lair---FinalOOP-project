# Ending Scene Usage

## Overview
The ending cutscene is a cinematic, emotional sequence that plays automatically after the player defeats the final boss. It features line-by-line dialogue with typewriter effects, controlled timing, and subtle visual effects.

## Victory Flow

### 1. Boss Defeat
When the final boss is defeated, the victory overlay appears showing:
- "THE LAIR" title
- Victory message: "Caesar was only the first host. The real trap was waiting in the courtyard."
- Story text explaining the ending
- **No user input required** - the prompt has been removed

### 2. Victory Overlay (5 seconds)
The victory screen displays for exactly 5 seconds, allowing players to read the victory message.

### 3. Screen Shake (2 seconds)
After 5 seconds, an intense screen shake effect begins:
- Duration: 2 seconds
- Intensity: 18 pixels
- **Victory overlay is hidden** to clearly show the game world shaking
- Signals the transition to the ending cutscene

### 4. Ending Cutscene
After the shake, the game automatically transitions to the ending scene with the protagonist's final monologue.

## Features

### Dialogue System
- **24 lines of dialogue** with individual timing control
- **Typewriter effect** with variable speed per line (0.038s - 0.055s per character)
- **Automatic pauses** between lines (0.8s - 3.0s depending on emotional weight)
- **No user interaction required** - fully automated sequence

### Visual Effects
- **Vignette effect** - gradually darkens edges of screen
- **Fade-in from black** - smooth transition at start
- **Purple tint** - appears when mentioning "inside me" (line 4)
- **Camera shake** - subtle shake during intense moments (lines 9-13)
- **Text shake** - slight shake on final "goodbye" line
- **Desaturated background** - dim, dark atmosphere

### Audio
- **Background music** - plays `Ending.mp3` at 35% volume throughout the entire scene
- **Gunshot sound** - plays `gunshot.mp3` at 85% volume after fade to black
- **Music stops** after gunshot

### Sequence Timeline

1. **Dialogue Phase** (0:00 - ~2:30)
   - 24 lines displayed one at a time
   - Each line types out character by character
   - Automatic pauses between lines
   - Background music (Ending.mp3) plays throughout

2. **Pause** (~2:30 - ~2:32.5)
   - 2.5 second pause after final dialogue line
   - Screen continues to show effects

3. **Goodbye** (~2:32.5 - ~2:34.5)
   - Text: "…goodbye." with slight shake
   - 2.0 second pause

4. **Cut to Black** (~2:34.5)
   - Instant cut to black screen
   - 0.5 second silence

5. **Gunshot** (~2:35)
   - Loud gunshot sound effect (gunshot.mp3)
   - Background music stops
   - 2.0 second pause

6. **The End** (~2:37)
   - "THE END" fades in slowly
   - Holds for 4 seconds
   - Application exits

## How to Trigger

The ending scene triggers automatically when you defeat the final boss. The sequence is:

1. **Victory overlay displays** (5 seconds)
2. **Screen shakes intensely** (2 seconds)
3. **Ending cutscene begins** (automatic transition)

### For Testing
You can test the ending scene by temporarily modifying `Main.java`:

```java
@Override
public void start(Stage stage) {
    GameContext.initialize(stage);
    // GameContext.showIntro();  // Comment out normal flow
    GameContext.showEnding();     // Test ending directly
}
```

## Dialogue Content

The ending features the protagonist's internal monologue reflecting on:
- The infection still inside them
- Regret over not stopping Caesar
- Memories of normal campus life
- Guilt over failing to save friends
- The decision to end it before becoming like Hunos

## Technical Details

### File Structure
- **Scene Class**: `src/main/java/org/example/ui/EndingScene.java`
- **Audio Assets**: 
  - `assets/ending/Ending.mp3` - background music
  - `assets/ending/gunshot.mp3` - gunshot effect
- **Asset Registry**: Audio registered as `audio.ending.bgm` and `audio.ending.gunshot`

### Customization

To adjust timing, edit the `DialogueLine` records in `EndingScene.java`:

```java
new DialogueLine("text", pauseAfter, typeSpeed)
// pauseAfter: seconds to wait after line completes
// typeSpeed: seconds per character (lower = faster)
```

To modify visual effects, adjust these values in `updateEffects()`:
- `vignetteIntensity` - darkness of edges (0.0 - 1.0)
- `purpleTint` - purple overlay intensity (0.0 - 1.0)
- `shakeX/shakeY` - camera shake magnitude

## Notes

- The scene automatically exits the application after completion
- No user input is accepted during the sequence
- All timing is carefully calibrated for emotional impact
- The scene cannot be skipped or fast-forwarded
