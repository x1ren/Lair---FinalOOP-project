# THE LAIR System Overview

## Project Summary

`THE LAIR` is a JavaFX action game built in Java.  
The player follows a story-driven progression through an infected school, selects a survivor with a fixed weapon loadout, fights enemy waves, defeats Caesar Hunos, and then faces the final boss, the false Sir Khai.

## Current Scope

The implemented system already covers these major areas:

- cinematic story introduction
- character selection
- fixed character loadouts
- gameplay scene with stage progression
- enemy encounters and boss encounters
- HUD and combat actions
- UML documentation for requirements and design

## Main Game Flow

1. Launch the application
2. View the story intro
3. Select a playable character
4. Enter the gameplay scene with the assigned gun
5. Clear infected stages across the campus
6. Defeat Caesar Hunos
7. Defeat the false Sir Khai
8. View the ending

## Main Packages

### `org.example.scenes`

Contains the JavaFX scenes that represent the game flow:

- `IntroScene`
- `CharacterSelectScene`
- `GameScene`

### `org.example.player`

Contains the playable character data and fixed loadout stats:

- `CharacterType`

### `org.example.weapons`

Contains the weapon hierarchy and weapon factory:

- `Weapon`
- `AssaultRifle`
- `SMG`
- `Shotgun`
- `Sniper`
- `LMG`
- `WeaponFactory`

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
- object-oriented modeling is used for players, weapons, enemies, stages, and managers
- inheritance, abstraction, encapsulation, polymorphism, and generics are present in the code

## Documentation Direction

The use case diagram is intentionally kept high-level, based on the lecture notes.  
More detail is provided through:

- textual use case specifications
- a class diagram
- OOP design notes
