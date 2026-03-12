# THE LAIR Use Case Notes

This version now follows the lecture notes more closely:

- A use case diagram is a high-level overview.
- It should show actors, use cases, the system boundary, and their relationships.
- It should not model every step or the exact order of actions.

## Main components used

- `Player` is the actor outside the system.
- `THE LAIR Game System` is the system boundary.
- The ovals are goal-level use cases of the game.

## Why this version is more proper

The earlier version was too detailed because it broke the game into many low-level steps.  
Based on the notes, a use case diagram should focus on user goals and system interactions, not every small action.

So the revised diagram keeps only the main goals:

- `Start New Game`
- `View Story Intro`
- `Select Character`
- `Play Game`
- `Clear Story Stages`
- `Clear Library`
- `Clear Canteen`
- `Clear Gym`
- `Clear Courtyard`
- `Use Character Skill`
- `Reload Weapon`
- `View Ending`
- `Exit Game`

## Relationships used

### Association

The `Player` is connected by simple lines to the major use cases:

- `Start New Game`
- `Play Game`
- `Exit Game`

This follows the note that association shows interaction between an actor and a use case.

### Include

`Start New Game` includes:

- `View Story Intro`
- `Select Character`

`Play Game` includes:

- `Clear Story Stages`
- `View Ending`

`Clear Story Stages` includes:

- `Clear Library`
- `Clear Canteen`
- `Clear Gym`
- `Clear Courtyard`

These are required parts of those larger use cases.

Because the current build assigns one weapon to each survivor automatically, weapon selection is no longer shown as a separate use case.

### Extend

`Use Character Skill` extends `Play Game`  
`Reload Weapon` extends `Play Game`

These are optional actions that may happen during gameplay, which matches the lecture note for `extend`.

The diagram no longer separates generic `Fight Minions` and `Fight Boss` use cases, because combat is already part of clearing each story stage. That keeps the model simpler and avoids repeating the same gameplay idea multiple times.

## Short explanation for reporting

“Our use case diagram presents a high-level overview of the interaction between the player and THE LAIR game system. The player is the main actor. The diagram uses association for direct interaction, include for required sub-functions, and extend for optional gameplay actions. It also shows the four required story stages as included parts of clearing the game. Character skills and weapon reloads are modeled as optional gameplay actions under Play Game. Character choice also determines the player’s fixed weapon loadout.”

## Related documents

This use case diagram is intentionally kept simple.  
More detail is documented in:

- [use-case-specifications.md](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/use-case-specifications.md)
- [class-diagram.puml](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/class-diagram.puml)
- [oop-design-notes.md](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/oop-design-notes.md)
- [system-overview.md](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/system-overview.md)
