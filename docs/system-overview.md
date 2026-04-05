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

- `CharacterType`

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
- `EnemyActor`
- `Projectile`
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
