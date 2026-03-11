# THE LAIR Use Case Specifications

These textual use case descriptions supplement the high-level use case diagram.

## UC-01 Start New Game

**Primary Actor:** Player  
**Goal:** Begin a new play session  
**Precondition:** Application is running  
**Postcondition:** The player reaches the selection and gameplay flow

**Basic Flow**

1. Player chooses to start a new game.
2. System shows the story introduction.
3. System proceeds to character selection.
4. System proceeds to weapon selection.
5. System starts the game.

## UC-02 Select Character

**Primary Actor:** Player  
**Goal:** Choose one survivor  
**Precondition:** New game was started  
**Postcondition:** Selected character is stored for the run

**Basic Flow**

1. Player reviews available survivors.
2. System displays the role, stats, and skills of the selected survivor.
3. Player confirms the character.

## UC-03 Select Weapon

**Primary Actor:** Player  
**Goal:** Choose one weapon  
**Precondition:** Character has already been selected  
**Postcondition:** Selected weapon is stored for the run

**Basic Flow**

1. Player reviews available weapons.
2. System displays weapon stats and description.
3. Player confirms the weapon.

## UC-04 Play Game

**Primary Actor:** Player  
**Goal:** Progress through the game story and complete the run  
**Precondition:** Character and weapon are selected  
**Postcondition:** Player reaches either victory or defeat ending

**Basic Flow**

1. Player enters the gameplay scene.
2. Player explores infected areas of the campus.
3. Player fights standard enemies.
4. Player reaches boss encounters.
5. Player views the ending.

## UC-05 Explore Campus

**Primary Actor:** Player  
**Goal:** Move through the infected school areas  
**Precondition:** Gameplay has started  
**Postcondition:** Player advances to the next stage or encounter

**Basic Flow**

1. Player moves through the current area.
2. System presents a themed stage.
3. Player clears the stage and progresses forward.

## UC-06 Fight Enemies

**Primary Actor:** Player  
**Goal:** Defeat infected enemies blocking progression  
**Precondition:** Player is inside gameplay  
**Postcondition:** Stage is cleared or the player is defeated

**Basic Flow**

1. System spawns enemies in the current stage.
2. Player attacks enemies using the equipped weapon.
3. Player may use a character skill.
4. Player may reload the weapon.
5. System removes defeated enemies.

## UC-07 Fight Boss

**Primary Actor:** Player  
**Goal:** Defeat a boss encounter  
**Precondition:** Player reached a boss stage  
**Postcondition:** Boss is defeated or player is defeated

**Specializations**

- `Fight Caesar Hunos`
- `Fight False Sir Khai`

## UC-08 View Ending

**Primary Actor:** Player  
**Goal:** See the outcome of the game run  
**Precondition:** Final sequence is reached  
**Postcondition:** Player may return to selection or exit

**Basic Flow**

1. System shows the ending overlay.
2. Player reads the result of the run.
3. Player decides whether to return or exit.

## UC-09 Exit Game

**Primary Actor:** Player  
**Goal:** Leave the application  
**Precondition:** Application is open  
**Postcondition:** Session ends
