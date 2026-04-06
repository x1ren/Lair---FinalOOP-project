# THE LAIR System Overview

## Project Summary

`THE LAIR` is a JavaFX action game built in Java.  
The player follows a story-driven progression through an infected school, selects one of the five survivors with a fixed weapon loadout, clears the Library, Canteen, Gym, and Courtyard stages, defeats Caesar Hunos, and then faces the final LAIR Mimic wearing Sir Khai's form.

## Current Scope

The implemented system already covers these major areas:

- cinematic story introduction
- character selection
- fixed character loadouts
- gameplay scene with stage progression
- four named story stages from the requirement document
- enemy encounters and boss encounters
- HUD and combat actions
- UML documentation for requirements and design

## Main Game Flow

1. Launch the application
2. View the story intro
3. Select a playable character
4. Enter the gameplay scene with the assigned gun
5. Use direct combat inputs for shooting, skill activation, and reloading
6. Clear the Library and defeat the Security Guard
7. Clear the Canteen and defeat the Mutated Vendor
8. Clear the Gym and defeat Caesar Hunos
9. Return to the Courtyard and defeat the LAIR Mimic
10. View the ending

## Main Packages

### `org.example.scenes`

Contains the JavaFX scenes that represent the game flow:

- `IntroScene`
- `CharacterSelectScene`
- `GameScene`

### `org.example.player`

Contains the playable character data and fixed loadout stats:

- `CharacterType` (base stats, weapon assignment, skill name, `getSkillEffectSummary()` for HUD)
- `CharacterCombatProfile` / `CharacterCombatProfiles` (data-driven ability tuning: bleed, slow, overdrive, focus, overload, stage scaling hooks)

### Combat UI and controls (gameplay)

| Input | Action |
|--------|--------|
| `A` / `D` or arrow keys | Move |
| Left mouse button | Fire |
| `Q` | Activate skill (per survivor; see cooldown bar) |
| `R` | Reload |
| `Space` | Jump |
| `Esc` | Return to character select |

The in-game HUD shows:

- **Skill**: skill name, `[Q]` binding, READY or cooldown time, a summary of what the skill does, and the cooldown bar.
- **ACTIVE EFFECTS & SCALING**: current **stage damage multiplier** (later stages deal more player damage), and **timed lines** for Hemorrhage, Suppress, Overdrive, Overload, and Focus when they are active.
- A **controls** reminder strip at the bottom of the screen.

Enemy feedback: slowed enemies get a blue tint; bleeding enemies get a red edge; adaptation states may show brief highlights (see `EnemyTuningState`).

### `org.example.runtime`

Contains the shared runtime context and app-level scene routing:

- `GameContext`

### `org.example.assets`

Contains runtime asset loading, sprite helpers, and background preloading:

- `AssetRegistry`
- `AssetPreloader`
- `SpriteSheet`
- `SpriteSet`

### `org.example.audio`

Contains lightweight sound playback coordination:

- `AudioManager`

### `org.example.weapons`

Contains the immutable weapon data model:

- `Weapon`
- `WeaponCatalog`
- `WeaponType`

### `org.example.gameplay`

Contains the reusable gameplay model and entity classes:

- `GameObject`
- `PlayerActor`
- `EnemyActor` (status effects, `EnemyTuningState` for light adaptive AI)
- `Projectile` with `HitPayload` / `StatusApplication` (bleed, slow on hit)
- `CombatScaling` (stage-based player damage multiplier)
- `PlatformTile`
- `StageDefinition`
- `EntityManager<T>`

### `org.example.engines`

Contains reusable engine support:

- `GameLoop`
- `InputHandler`
- `CollisionManager`

## GUI and OOP Status

The project already demonstrates GUI and OOP in a meaningful way:

- JavaFX scenes provide the GUI flow
- runtime asset loading is centralized instead of being scattered in scenes
- object-oriented modeling is used for players, weapons, enemies, stages, assets, and managers
- inheritance, abstraction, encapsulation, polymorphism, and generics are present in practical, limited ways

## Documentation Direction

The use case diagram is intentionally kept high-level, based on the lecture notes.  
More detail is provided through:

- textual use case specifications
- a class diagram
- OOP design notes
