# THE LAIR Documentation

This folder keeps the project documentation high-level and presentation-ready.

## Run The Game

From the project root, start the game with:

```bash
mvn javafx:run
```

Requirements:

- Java 21
- Maven 3.9+ recommended

If Maven dependencies are not downloaded yet, the first run may take a bit longer.

### Debug: jump straight to a stage

For faster iteration you can skip the title flow and open `GameScene` directly. The run username is fixed to **`debug`**, and the default survivor is **Joseph Jimenez** unless you override it.

Stage numbers are **1-based** (1 = first story stage in `StageCatalog`, same order as the normal run).

**Option A — Maven property (no quoting issues)**

```bash
mvn javafx:run -Dlair.stage=3
```

This sets JVM system property `lair.stage`; the JavaFX plugin forwards it via `pom.xml`.

**Option B — application arguments**

```bash
mvn javafx:run -Dlair.cmd.args="--stage 3"
```

Optional second flag (must match a `CharacterType` enum name):

```bash
mvn javafx:run -Dlair.cmd.args="--stage 4 --debug-character ILDE_JAN_FIGUERAS"
```

Equivalent forms: `--stage=3`, `--debug-character=JOSEPH_JIMENEZ`.

Implementation lives in `org.example.app.LaunchConfig` and `GameContext.enterGameFromDebugShortcut`.

## Controls (in-game)

| Key | Action |
|-----|--------|
| Move | `A` `D` or arrow keys |
| Shoot | Left mouse button |
| **Skill** | **`Q`** (per character: Hemorrhage, Suppress, Overdrive, Overload, Focus) |
| Reload | `R` |
| Jump | `Space` |
| Pause menu | `Esc` |
| Back to main menu | Click **Back to Main Menu** from pause menu |

The HUD shows the **skill name**, **`[Q]`** hint, cooldown (`READY` or seconds), a short **effect summary**, and an **Active effects & scaling** panel (stage damage multiplier plus any buffs in progress). See [system-overview.md](system-overview.md) for packages and architecture.

## Files

- [use-case-diagram.puml](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/use-case-diagram.puml)
  High-level use case diagram based on the lecture notes.

- [use-case-notes.md](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/use-case-notes.md)
  Short explanation of the use case diagram and its UML relationships.

- [use-case-specifications.md](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/use-case-specifications.md)
  Textual descriptions of the major use cases.

- [system-overview.md](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/system-overview.md)
  Project scope, game flow, and implemented features.

- [class-diagram.puml](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/class-diagram.puml)
  UML class diagram for the current OOP design.

- [oop-design-notes.md](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/oop-design-notes.md)
  Notes explaining how the four OOP concepts and generics appear in the codebase.

## Recommended submission order

1. Use Case Diagram
2. Use Case Specifications
3. Class Diagram
4. OOP Design Notes
5. System Overview
