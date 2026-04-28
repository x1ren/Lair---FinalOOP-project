# THE LAIR - Final UML Diagrams

## Overview

This project includes two UML diagrams that document the system design:

1. **Use Case Diagram** - High-level player interactions
2. **Class Diagram** - Core OOP architecture (7 essential classes)

---

## 1. Use Case Diagram

**File:** `docs/use-case-diagram.puml`

### Purpose
Shows how the Player interacts with THE LAIR Game System at a high level.

### Components
- **1 Actor:** Player
- **13 Use Cases:** Game flow from start to ending
- **3 Relationship Types:**
  - Association (—): Direct player interaction
  - Include (<<include>>): Required sub-functions
  - Extend (<<extend>>): Optional gameplay actions

### Main Flow
```
Start New Game
  ├─ View Story Intro (included)
  └─ Select Character (included)
     └─ Play Game
          ├─ Clear Story Stages (included)
          │    ├─ Clear Library (included)
          │    ├─ Clear Canteen (included)
          │    ├─ Clear Gym (included)
          │    └─ Clear Courtyard (included)
          ├─ View Ending (included)
          ├─ Use Character Skill (extends)
          └─ Reload Weapon (extends)
```

---

## 2. Class Diagram (Simplified)

**File:** `docs/class-diagram.puml`

### Purpose
Demonstrates OOP concepts through 7 core classes that form the game's architecture.

### The 7 Classes

#### 1. Main
- **Role:** Application entry point
- **OOP Concept:** Encapsulation
- **Key Features:** Static constants (WIDTH, HEIGHT, TITLE), main method

#### 2. GameLoop (Abstract)
- **Role:** Game engine template
- **OOP Concepts:** Abstraction, Template Method Pattern
- **Key Features:** Abstract update() and render() methods, FPS tracking
- **Why Abstract:** Defines structure but lets subclasses implement specific behavior

#### 3. GameObject (Abstract)
- **Role:** Base class for all game entities
- **OOP Concepts:** Abstraction, Encapsulation
- **Key Features:** Position (x, y), size (width, height), abstract render()
- **Why Abstract:** Common properties for all entities, but each renders differently

#### 4. PlayerActor
- **Role:** Player character entity
- **OOP Concepts:** Inheritance, Polymorphism, Encapsulation
- **Extends:** GameObject
- **Key Features:** Physics (velocity), animation, health, implements render()

#### 5. EnemyActor
- **Role:** Enemy character entity
- **OOP Concepts:** Inheritance, Polymorphism, Association
- **Extends:** GameObject
- **Key Features:** Health, damage, AI (targets PlayerActor), implements render()

#### 6. GameScene
- **Role:** Main game controller
- **OOP Concepts:** Inheritance, Composition
- **Extends:** GameLoop
- **Contains:** PlayerActor, EntityManager instances
- **Key Features:** Orchestrates gameplay, implements update() and render()

#### 7. EntityManager<T extends GameObject>
- **Role:** Generic entity collection manager
- **OOP Concepts:** Generics, Polymorphism, Type Safety
- **Generic Constraint:** T must extend GameObject
- **Key Features:** Type-safe add/remove/update/render for any GameObject type

### Relationships

| From | To | Type | Meaning |
|------|-----|------|---------|
| GameScene | GameLoop | Inheritance (--|>) | GameScene extends GameLoop |
| PlayerActor | GameObject | Inheritance (--|>) | PlayerActor extends GameObject |
| EnemyActor | GameObject | Inheritance (--|>) | EnemyActor extends GameObject |
| GameScene | PlayerActor | Composition (*--) | GameScene contains PlayerActor |
| GameScene | EntityManager | Composition (*--) | GameScene contains EntityManager |
| EnemyActor | PlayerActor | Association (-->) | EnemyActor targets PlayerActor |
| Main | GameScene | Association (-->) | Main creates GameScene |
| EntityManager | GameObject | Dependency (..>) | EntityManager manages GameObject types |

### OOP Concepts Demonstrated

1. **Encapsulation**
   - All classes use private fields with public methods
   - Example: GameObject's x, y, width, height are protected/private

2. **Abstraction**
   - GameLoop defines game loop structure without implementation
   - GameObject defines entity contract without specific rendering

3. **Inheritance**
   - GameScene inherits from GameLoop
   - PlayerActor and EnemyActor inherit from GameObject
   - Promotes code reuse and establishes "is-a" relationships

4. **Polymorphism**
   - EntityManager<T> works with any GameObject subtype
   - render() method overridden in PlayerActor and EnemyActor
   - Allows treating different entities uniformly

5. **Generics**
   - EntityManager<T extends GameObject> provides type safety
   - Can create EntityManager<PlayerActor>, EntityManager<EnemyActor>, etc.
   - Compile-time type checking prevents errors

---

## Why These 7 Classes?

These classes were carefully selected because they:

1. **Cover all OOP concepts** required for academic projects
2. **Show real-world design patterns** (Template Method, Composition)
3. **Demonstrate class relationships** (inheritance, composition, association)
4. **Are essential to the game** - the game cannot function without them
5. **Are simple enough** to understand but complex enough to be meaningful
6. **Show progression** from abstract to concrete implementations

---

## How to View the Diagrams

### Option 1: Online (Easiest)
1. Go to http://www.plantuml.com/plantuml/uml/
2. Open the `.puml` file in a text editor
3. Copy all content
4. Paste into the online editor
5. View the generated diagram

### Option 2: VS Code
1. Install "PlantUML" extension
2. Open the `.puml` file
3. Press `Alt+D` to preview
4. Or right-click → "Preview Current Diagram"

### Option 3: IntelliJ IDEA
1. Install "PlantUML integration" plugin
2. Open the `.puml` file
3. Diagram appears automatically in side panel
4. Or right-click → "Show PlantUML Diagram"

### Option 4: Command Line
```bash
# Install PlantUML first (requires Java)
# Download from: https://plantuml.com/download

# Generate PNG images
plantuml docs/use-case-diagram.puml
plantuml docs/class-diagram.puml

# This creates:
# - docs/use-case-diagram.png
# - docs/class-diagram.png
```

---

## Diagram Statistics

### Use Case Diagram
- Actors: 1
- Use Cases: 13
- Relationships: 15
- Complexity: Medium (appropriate for academic presentation)

### Class Diagram
- Classes: 7
- Abstract Classes: 2
- Concrete Classes: 5
- Inheritance Relationships: 3
- Composition Relationships: 2
- Association Relationships: 2
- Generic Types: 1
- Complexity: Simple but comprehensive

---

## Related Documentation

For more details, see:

- **DIAGRAMS_SUMMARY.md** - Comprehensive overview of both diagrams
- **QUICK_DIAGRAM_REFERENCE.md** - Quick reference with class details
- **oop-design-notes.md** - How OOP concepts are used in the codebase
- **system-overview.md** - Overall project architecture
- **use-case-notes.md** - Explanation of use case relationships
- **use-case-specifications.md** - Detailed textual use case descriptions

---

## Tips for Presentation

### For Use Case Diagram
1. Start with the actor (Player)
2. Explain the main flow (Start → Select → Play → End)
3. Point out the 4 story stages (Library, Canteen, Gym, Courtyard)
4. Explain include vs extend relationships
5. Keep it high-level - don't dive into implementation

### For Class Diagram
1. Start with the abstract classes (GameLoop, GameObject)
2. Show the inheritance hierarchy
3. Explain how GameScene orchestrates everything
4. Highlight the generic EntityManager
5. Point out each OOP concept with examples
6. Use the notes in the diagram to guide your explanation

### Common Questions & Answers

**Q: Why only 7 classes?**
A: These 7 classes demonstrate all required OOP concepts while remaining clear and understandable. More classes would add complexity without adding educational value.

**Q: Where are the other classes mentioned in the code?**
A: The full system has 40+ classes, but this diagram focuses on the core architecture. Other classes support these core classes.

**Q: Why are GameLoop and GameObject abstract?**
A: They define contracts and common behavior without implementation details. This is the essence of abstraction - defining "what" without "how".

**Q: What's the benefit of EntityManager<T>?**
A: Type safety and code reuse. One generic class can manage any type of GameObject, preventing type errors at compile time.

**Q: How does this show polymorphism?**
A: Multiple ways: (1) EntityManager works with any GameObject subtype, (2) render() is overridden in subclasses, (3) GameScene treats all entities uniformly.

---

## Validation Checklist

Before submitting, verify:

- [ ] Both `.puml` files are syntactically correct
- [ ] Diagrams render without errors
- [ ] All 5 OOP concepts are clearly demonstrated
- [ ] Relationships are properly labeled
- [ ] Class attributes and methods are shown
- [ ] Notes explain key concepts
- [ ] Documentation files are complete
- [ ] File paths are correct in documentation

---

## Final Notes

These diagrams represent a simplified but accurate view of THE LAIR's architecture. They focus on demonstrating OOP principles rather than documenting every implementation detail. This approach makes them ideal for academic presentations while still being technically accurate.

The diagrams are maintained in PlantUML format for easy version control and updates. As the codebase evolves, these diagrams can be updated by editing the text files rather than recreating visual diagrams from scratch.
