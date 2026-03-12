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
- `Explore Campus`
- `Fight Enemies`
- `Fight Boss`
- `Fight Caesar Hunos`
- `Fight False Sir Khai`
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

- `Explore Campus`
- `Fight Enemies`
- `Fight Boss`
- `View Ending`

These are required parts of those larger use cases.

Because the current build assigns one weapon to each survivor automatically, weapon selection is no longer shown as a separate use case.

### Extend

`Use Character Skill` extends `Fight Enemies`  
`Reload Weapon` extends `Fight Enemies`

These are optional actions that may happen during combat, which matches the lecture note for `extend`.

### Generalization

`Fight Caesar Hunos` and `Fight False Sir Khai` are specialized forms of `Fight Boss`.

This matches the note that generalization represents an “is-a” relationship.

## Short explanation for reporting

“Our use case diagram presents a high-level overview of the interaction between the player and THE LAIR game system. The player is the main actor. The diagram uses association for direct interaction, include for required sub-functions, extend for optional combat actions, and generalization to show that the two boss fights are specialized versions of a general boss fight. Character choice also determines the player’s fixed weapon loadout.” 

## Related documents

This use case diagram is intentionally kept simple.  
More detail is documented in:

- [use-case-specifications.md](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/use-case-specifications.md)
- [class-diagram.puml](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/class-diagram.puml)
- [oop-design-notes.md](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/oop-design-notes.md)
- [system-overview.md](/Users/gailleamolong/Documents/School/OOP2/Lair---FinalOOP-project/docs/system-overview.md)
