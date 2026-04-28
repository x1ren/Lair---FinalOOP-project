# THE LAIR - Diagram Preview

## Use Case Diagram

The use case diagram shows player interactions with the game system at a high level.

**Key Features:**
- 1 Actor: Player
- 13 Use Cases organized hierarchically
- 3 Relationship Types: Association, Include, Extend

**Main Flow:**
```
Player starts game → Views intro → Selects character → 
Plays through 4 stages → Views ending
```

**Optional Actions:**
- Use Character Skill (Q key)
- Reload Weapon (R key)

---

## Class Diagram

The class diagram shows the complete object-oriented architecture with 9 packages and 50+ classes.

### Package Structure

#### 1. Main Application (`org.example`)
- **Main**: Entry point with JavaFX Application

#### 2. Scenes (`org.example.scenes`)
- **IntroScene**: Story introduction
- **CharacterSelectScene**: Character selection UI
- **GameScene**: Main gameplay (extends GameLoop)
- **StageArena**: Stage progression manager
- **GameHudRenderer**: HUD rendering
- **GameVisualRenderer**: Game world rendering

#### 3. Player System (`org.example.player`)
- **CharacterType** (enum): 5 playable characters
  - Joseph Jimenez (Assault Rifle, Hemorrhage)
  - Iben Anoos (LMG, Suppress)
  - Ilde Jan Figueras (SMG, Overdrive)
  - Gaille Amolong (Shotgun, Overload)
  - Jamuel Bacus (Sniper, Focus)
- **CharacterCombatProfile**: Ability stats
- **CharacterCombatProfiles**: Profile factory

#### 4. Runtime (`org.example.runtime`)
- **GameContext**: Singleton managing app state, assets, audio, scenes

#### 5. Assets (`org.example.assets`)
- **AssetRegistry**: Central asset repository
- **AssetPreloader**: Background asset loading
- **SpriteSheet**: Sprite frame container
- **SpriteSet**: Animation manager
- **AnimationStrip**: Single animation sequence
- **AnimationState** (enum): IDLE, WALK, JUMP, FALL, HIT, DEATH

#### 6. Audio (`org.example.audio`)
- **AudioManager**: Sound effect playback

#### 7. Engines (`org.example.engines`)
- **GameLoop** (abstract): Frame-based game loop with update/render
- **InputHandler**: Keyboard and mouse input
- **CollisionManager**: AABB collision detection

#### 8. Gameplay (`org.example.gameplay`)

**Core Entities:**
- **GameObject** (abstract): Base class with position, size, render
  - **PlayerActor**: Player character with physics, animation, combat
  - **EnemyActor**: Enemy with AI, health, status effects
  - **Projectile**: Bullets with damage and status payloads
  - **PlatformTile**: Solid platforms
  - **StageExitMarker**: Stage transition trigger

**Entity Management:**
- **EntityManager<T>**: Generic collection manager for game objects

**Stage System:**
- **StageDefinition**: Stage data (name, mob count, boss info)
- **StageCatalog**: Stage factory

**Combat System:**
- **HitPayload**: Damage + status effects
- **StatusApplication**: Status effect instance
- **StatusType** (enum): BLEED, SLOW
- **BleedConfig**: Damage over time configuration
- **SlowConfig**: Movement speed reduction
- **CombatScaling**: Stage-based damage multipliers
- **EnemyTuningState** (enum): AI adaptation states

#### 9. Weapons (`org.example.weapons`)
- **Weapon**: Weapon stats (fire rate, magazine, reload, etc.)
- **WeaponCatalog**: Weapon factory
- **WeaponType** (enum): ASSAULT_RIFLE, SMG, SHOTGUN, SNIPER, LMG

### Key Design Patterns

1. **Abstract Factory Pattern**
   - WeaponCatalog creates Weapon instances
   - CharacterCombatProfiles creates CharacterCombatProfile instances
   - StageCatalog creates StageDefinition instances

2. **Template Method Pattern**
   - GameLoop defines update/render template
   - GameScene implements specific behavior

3. **Singleton Pattern**
   - GameContext provides global access to shared resources

4. **Generic Type Pattern**
   - EntityManager<T extends GameObject> for type-safe collections

5. **Enum Pattern**
   - CharacterType, WeaponType, AnimationState, StatusType, EnemyTuningState

6. **Composition over Inheritance**
   - GameScene contains StageArena, GameHudRenderer, GameVisualRenderer
   - PlayerActor contains SpriteSet
   - HitPayload contains StatusApplication list

### Inheritance Hierarchy

```
GameObject (abstract)
├── PlayerActor
│   ├── Physics (vx, vy, onGround)
│   ├── Animation (spriteSet, animationTime)
│   └── Combat (health, defeated)
│
├── EnemyActor
│   ├── AI (target, tuningState)
│   ├── Health (health, maxHealth)
│   └── Status Effects (bleed, slow)
│
├── Projectile
│   ├── Movement (vx, vy)
│   └── Damage (payload)
│
├── PlatformTile
│   └── Collision (solid)
│
└── StageExitMarker
    └── Trigger (proximity check)
```

### Data Flow

```
Main
  └─> GameContext.initialize()
        ├─> AssetRegistry (loads sprites, sounds)
        ├─> AudioManager (manages audio)
        └─> AssetPreloader (background loading)

GameContext.showIntro()
  └─> IntroScene
        └─> GameContext.showCharacterSelect()

GameContext.showCharacterSelect()
  └─> CharacterSelectScene
        └─> GameContext.showGame(character)

GameContext.showGame(character)
  └─> GameScene
        ├─> CharacterType.createWeapon()
        ├─> CharacterType.getCombatProfile()
        ├─> StageCatalog.getStage()
        ├─> EntityManager<PlayerActor>
        ├─> EntityManager<EnemyActor>
        ├─> EntityManager<Projectile>
        └─> EntityManager<PlatformTile>
```

### Combat Flow

```
Player fires weapon
  └─> GameScene.fireWeapon()
        └─> Create Projectile with HitPayload
              ├─> Base damage from CharacterType
              ├─> Multiplier from CombatScaling
              └─> StatusApplication (if ability active)

Projectile hits enemy
  └─> EnemyActor.takeDamage(payload)
        ├─> Apply damage
        ├─> Apply status effects
        │     ├─> BLEED (BleedConfig)
        │     └─> SLOW (SlowConfig)
        └─> Update EnemyTuningState
```

### Animation Flow

```
PlayerActor.render()
  └─> resolveAnimationState()
        ├─> DEATH (if defeated)
        ├─> HIT (if recently hit)
        ├─> JUMP (if airborne, vy < 0)
        ├─> FALL (if airborne, vy >= 0)
        ├─> WALK (if moving)
        └─> IDLE (default)
  └─> SpriteSet.draw(state, time)
        └─> AnimationStrip.getFrameAt(time)
              └─> SpriteSheet.getFrame(index)
```

## Diagram Statistics

### Use Case Diagram
- **Actors:** 1 (Player)
- **Use Cases:** 13
- **Relationships:** 15 (3 associations, 9 includes, 2 extends)

### Class Diagram
- **Packages:** 9
- **Classes:** 35
- **Enums:** 6
- **Records:** 1
- **Abstract Classes:** 2
- **Relationships:** 60+
- **Inheritance:** 5 (GameObject hierarchy)
- **Generics:** 1 (EntityManager<T>)

## How to Use These Diagrams

### For Development
1. **Adding new features:** Check class diagram for where to add code
2. **Understanding flow:** Follow use case diagram for user journey
3. **Refactoring:** Use diagrams to identify coupling and dependencies
4. **Code review:** Verify changes align with documented architecture

### For Documentation
1. **Onboarding:** New developers can understand system quickly
2. **Design discussions:** Visual reference for architecture decisions
3. **Requirements:** Use case diagram maps to user stories
4. **Testing:** Identify test scenarios from use cases

### For Presentation
1. **High-level overview:** Use case diagram for stakeholders
2. **Technical details:** Class diagram for technical audience
3. **OOP concepts:** Demonstrate inheritance, polymorphism, etc.
4. **Design patterns:** Show factory, singleton, template method patterns

## Next Steps

1. **Generate images:** Use PlantUML to create PNG/SVG files
2. **Update regularly:** Keep diagrams in sync with code changes
3. **Add sequence diagrams:** Show runtime interactions
4. **Add state diagrams:** Document character/enemy states
5. **Create deployment diagram:** Show runtime architecture

## Tools for Viewing

- **Online:** http://www.plantuml.com/plantuml/uml/
- **VS Code:** PlantUML extension
- **IntelliJ IDEA:** PlantUML integration plugin
- **Command line:** `plantuml docs/*.puml`
