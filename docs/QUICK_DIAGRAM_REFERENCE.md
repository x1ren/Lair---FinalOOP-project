# Quick Diagram Reference

## Use Case Diagram Overview

```
Player
  |
  |-- Start New Game
  |     |-- <<include>> View Story Intro
  |     |-- <<include>> Select Character
  |
  |-- Play Game
  |     |-- <<include>> Clear Story Stages
  |     |     |-- <<include>> Clear Library
  |     |     |-- <<include>> Clear Canteen
  |     |     |-- <<include>> Clear Gym
  |     |     |-- <<include>> Clear Courtyard
  |     |
  |     |-- <<include>> View Ending
  |     |-- <<extend>> Use Character Skill
  |     |-- <<extend>> Reload Weapon
  |
  |-- Exit Game
```

## Class Diagram - The 7 Core Classes

### 1. Main (Entry Point)
```
Main
  - WIDTH, HEIGHT, TITLE (constants)
  - start(Stage)
  - main(String[])
```
**OOP Concept:** Encapsulation (static constants and methods)

### 2. GameLoop (Abstract Engine)
```
GameLoop (abstract)
  - lastTime, running
  + start(), stop()
  # update(deltaTime) [abstract]
  # render() [abstract]
```
**OOP Concepts:** Abstraction, Template Method Pattern

### 3. GameObject (Abstract Base)
```
GameObject (abstract)
  # x, y, width, height
  + getX(), getY(), getWidth(), getHeight()
  + getCenterX(), getCenterY()
  + moveBy(dx, dy)
  + render(gc) [abstract]
```
**OOP Concepts:** Abstraction, Encapsulation

### 4. PlayerActor (Player Entity)
```
PlayerActor extends GameObject
  - vx, vy, facing, onGround
  - defeated, spriteSet
  + step(dt)
  + updateAnimation(dt)
  + triggerHit()
  + markDefeated()
  + render(gc) [override]
```
**OOP Concepts:** Inheritance, Polymorphism, Encapsulation

### 5. EnemyActor (Enemy Entity)
```
EnemyActor extends GameObject
  - health, maxHealth, damage
  - moveSpeed, target
  + takeDamage(payload)
  + applyStatus(status)
  + isSlowed(), isBleeding()
  + render(gc) [override]
```
**OOP Concepts:** Inheritance, Polymorphism, Association (targets PlayerActor)

### 6. GameScene (Game Controller)
```
GameScene extends GameLoop
  - player: PlayerActor
  - weapon, character
  - enemies: EntityManager<EnemyActor>
  - projectiles: EntityManager<Projectile>
  + GameScene(character)
  + getScene()
  - updateGame(dt) [override]
  - renderGame() [override]
  - fireWeapon()
  - activateAbility()
```
**OOP Concepts:** Inheritance, Composition, Orchestration

### 7. EntityManager<T> (Generic Manager)
```
EntityManager<T extends GameObject>
  - entities: List<T>
  + add(entity: T)
  + remove(entity: T)
  + getAll(): List<T>
  + update(dt)
  + render(gc)
```
**OOP Concepts:** Generics, Polymorphism, Type Safety

## OOP Concepts Demonstrated

1. **Encapsulation:** Private fields with public getters (all classes)
2. **Abstraction:** Abstract classes define contracts (GameLoop, GameObject)
3. **Inheritance:** PlayerActor and EnemyActor extend GameObject; GameScene extends GameLoop
4. **Polymorphism:** EntityManager<T> works with any GameObject subtype; render() method overridden
5. **Generics:** EntityManager<T extends GameObject> provides type-safe entity management

## Key Relationships

### Inheritance Hierarchy
```
GameLoop (abstract)
  └── GameScene

GameObject (abstract)
  ├── PlayerActor
  └── EnemyActor
```

### Composition
```
GameScene
  ├── contains PlayerActor
  └── contains EntityManager<EnemyActor>
```

### Association
```
EnemyActor --> PlayerActor (targets)
Main --> GameScene (creates)
```

### Generic Constraint
```
EntityManager<T extends GameObject>
  └── manages any GameObject subtype
```

## File Locations

- Use Case Diagram: `docs/use-case-diagram.puml`
- Class Diagram: `docs/class-diagram.puml`
- Diagram Summary: `docs/DIAGRAMS_SUMMARY.md`
- This Reference: `docs/QUICK_DIAGRAM_REFERENCE.md`

## Diagram Statistics

### Use Case Diagram
- **Actors:** 1 (Player)
- **Use Cases:** 13
- **Relationships:** 15 (3 associations, 9 includes, 2 extends)

### Class Diagram (Simplified)
- **Total Classes:** 7 core classes
- **Abstract Classes:** 2 (GameLoop, GameObject)
- **Concrete Classes:** 5 (Main, GameScene, PlayerActor, EnemyActor, EntityManager)
- **Generics:** 1 (EntityManager<T extends GameObject>)
- **Inheritance Relationships:** 3 (GameScene→GameLoop, PlayerActor→GameObject, EnemyActor→GameObject)
- **Composition Relationships:** 2 (GameScene contains PlayerActor and EntityManager)
- **Association Relationships:** 2 (EnemyActor→PlayerActor, Main→GameScene)

## Viewing Instructions

### Online (Easiest)
1. Go to http://www.plantuml.com/plantuml/uml/
2. Copy the content from the `.puml` file
3. Paste and view

### VS Code
1. Install "PlantUML" extension
2. Open `.puml` file
3. Press `Alt+D` to preview

### IntelliJ IDEA
1. Install "PlantUML integration" plugin
2. Right-click `.puml` file
3. Select "Show PlantUML Diagram"

### Command Line
```bash
# Install PlantUML first
# Then generate PNG images:
plantuml docs/use-case-diagram.puml
plantuml docs/class-diagram.puml
```

## Key Relationships in Class Diagram

| Relationship | Symbol | Example |
|--------------|--------|---------|
| Inheritance | `--|>` | PlayerActor --|> GameObject |
| Composition | `*--` | GameScene *-- StageArena |
| Association | `-->` | Main --> GameContext |
| Dependency | `..>` | IntroScene ..> GameContext |
| Generics | `<T>` | EntityManager<T extends GameObject> |

## Stage Progression

1. **Library** → Security Guard (Boss)
2. **Canteen** → Mutated Vendor (Boss)
3. **Gym** → Caesar Hunos (Boss)
4. **Courtyard** → LAIR Mimic (Final Boss)
