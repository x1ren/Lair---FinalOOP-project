# THE LAIR OOP Design Notes

## Encapsulation

Encapsulation is used by hiding internal state and exposing only methods needed by other classes.

Examples:

- [Weapon.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/weapons/Weapon.java) keeps weapon fields private and exposes getters.
- [CharacterType.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/player/CharacterType.java) keeps each survivor's fixed loadout stats and asset mapping in one enum with getters.
- [GameObject.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/gameplay/GameObject.java) keeps position and size inside the object.
- [PlayerActor.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/gameplay/PlayerActor.java) and [EnemyActor.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/gameplay/EnemyActor.java) manage their own state.
- [AssetRegistry.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/assets/AssetRegistry.java) hides raw resource paths behind logical asset IDs.

## Abstraction

Abstraction is used to define common behavior without exposing every implementation detail.

Examples:

- [GameLoop.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/engine/GameLoop.java) defines abstract `update()` and `render()` methods.
- [GameObject.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/gameplay/GameObject.java) defines a shared gameplay object abstraction.
- [Weapon.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/weapons/Weapon.java) defines a single reusable weapon data model.
- [CharacterType.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/player/CharacterType.java) abstracts each survivor into one reusable selection unit.
- [GameContext.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/app/GameContext.java) abstracts shared runtime systems behind one access point.

## Inheritance

Inheritance is used where specialized classes extend a more general class.

Examples:

- `PlayerActor`, `EnemyActor`, `Projectile`, and `PlatformTile` extend `GameObject`.

## Polymorphism

Polymorphism is used when different concrete classes are handled through a shared parent type.

Examples:

- `EntityManager<T extends GameObject>` can render and manage different gameplay object types through their shared base class behavior.
- `GameScene` works with `Weapon` and gameplay base abstractions instead of hard-coding each class separately.
- `SpriteSet` chooses animation strips through the shared `AnimationState` enum instead of hard-coded per-frame drawing in the scene.

## Generics

Generics are used to keep code reusable and type-safe.

Examples:

- [EntityManager.java](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/src/main/java/org/example/gameplay/EntityManager.java) uses `EntityManager<T extends GameObject>`.

## Why this matters in the project

These OOP concepts make the game easier to:

- organize
- extend
- maintain
- document in UML
