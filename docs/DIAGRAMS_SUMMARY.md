# THE LAIR - UML Diagrams Summary

This document provides an overview of the UML diagrams created for THE LAIR project.

## Available Diagrams

### 1. Use Case Diagram (`use-case-diagram.puml`)

The use case diagram shows the high-level interactions between the Player and THE LAIR Game System.

**Key Components:**
- **Actor:** Player
- **System Boundary:** THE LAIR Game System
- **Main Use Cases:**
  - Start New Game (includes View Story Intro, Select Character)
  - Play Game (includes Clear Story Stages, View Ending)
  - Clear Story Stages (includes Clear Library, Clear Canteen, Clear Gym, Clear Courtyard)
  - Use Character Skill (extends Play Game)
  - Reload Weapon (extends Play Game)
  - Exit Game

**Relationships:**
- **Association (—):** Direct interaction between Player and use cases
- **Include (<<include>>):** Required sub-functions that are always part of the base use case
- **Extend (<<extend>>):** Optional actions that may occur during gameplay

### 2. Class Diagram (`class-diagram.puml`)

The class diagram shows the 7 most important classes that demonstrate the core OOP architecture and design patterns.

**The 7 Key Classes:**

1. **Main** - Application entry point
   - Demonstrates encapsulation with static constants
   - Launches the JavaFX application

2. **GameLoop** (abstract) - Game engine template
   - Demonstrates abstraction and template method pattern
   - Defines update/render cycle structure
   - Subclasses implement specific behavior

3. **GameObject** (abstract) - Base entity class
   - Demonstrates abstraction and encapsulation
   - Defines common properties (position, size)
   - Abstract render method for polymorphism

4. **PlayerActor** - Player character
   - Demonstrates inheritance (extends GameObject)
   - Encapsulates player state (velocity, animation, health)
   - Concrete implementation of render method

5. **EnemyActor** - Enemy character
   - Demonstrates inheritance (extends GameObject)
   - Shows object relationships (targets PlayerActor)
   - Implements combat and AI behavior

6. **GameScene** - Main game controller
   - Demonstrates inheritance (extends GameLoop)
   - Shows composition (contains PlayerActor, EntityManager)
   - Orchestrates all game entities and logic

7. **EntityManager<T>** - Generic entity manager
   - Demonstrates generics (T extends GameObject)
   - Shows polymorphism (manages any GameObject type)
   - Type-safe collection management

**OOP Concepts Demonstrated:**
- **Encapsulation:** Private fields with public methods (all classes)
- **Abstraction:** Abstract classes (GameLoop, GameObject)
- **Inheritance:** GameObject → PlayerActor, EnemyActor; GameLoop → GameScene
- **Polymorphism:** EntityManager works with any GameObject subtype
- **Generics:** EntityManager<T extends GameObject>

**Key Relationships:**
- **Inheritance (--|>):** PlayerActor/EnemyActor extend GameObject; GameScene extends GameLoop
- **Composition (*--):** GameScene contains PlayerActor and EntityManager
- **Association (-->):** EnemyActor targets PlayerActor; Main creates GameScene
- **Dependency (..>):** EntityManager manages GameObject types

## Viewing the Diagrams

To view these PlantUML diagrams, you can:

1. **Use PlantUML online:** Copy the content to http://www.plantuml.com/plantuml/uml/
2. **Use VS Code:** Install the "PlantUML" extension
3. **Use IntelliJ IDEA:** Install the "PlantUML integration" plugin
4. **Command line:** Install PlantUML and run:
   ```bash
   plantuml docs/use-case-diagram.puml
   plantuml docs/class-diagram.puml
   ```

## Related Documentation

- [use-case-notes.md](use-case-notes.md) - Detailed explanation of use case relationships
- [use-case-specifications.md](use-case-specifications.md) - Textual use case descriptions
- [oop-design-notes.md](oop-design-notes.md) - OOP concepts implementation
- [system-overview.md](system-overview.md) - Project scope and architecture
- [README.md](README.md) - Main documentation index

## Why These 7 Classes?

These classes were chosen because they:
1. **Represent core architecture** - Entry point, game loop, entities, scene
2. **Demonstrate all OOP concepts** - Encapsulation, abstraction, inheritance, polymorphism, generics
3. **Show design patterns** - Template method, composition, type safety
4. **Illustrate relationships** - Inheritance hierarchy, object associations, generic constraints
5. **Are essential to the game** - Without these, the game cannot function

## Diagram Maintenance

When updating the codebase:
- Keep the diagram focused on these 7 core classes
- Update relationships when dependencies change
- Keep use cases high-level (don't add implementation details)
- Document significant changes in the related markdown files
