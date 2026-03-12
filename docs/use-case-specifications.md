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
4. System assigns the selected character's fixed weapon loadout.
5. System starts the game.

## UC-02 Select Character

**Primary Actor:** Player  
**Goal:** Choose one survivor  
**Precondition:** New game was started  
**Postcondition:** Selected character is stored for the run

**Basic Flow**

1. Player reviews available survivors.
2. System displays the role, fixed weapon, stats, and skill of the selected survivor.
3. Player confirms the character.
4. System stores the survivor and assigned weapon for the run.

## UC-03 Play Game

**Primary Actor:** Player  
**Goal:** Progress through the game story and complete the run  
**Precondition:** A character is selected and the assigned loadout is active  
**Postcondition:** Player reaches either victory or defeat ending

**Basic Flow**

1. Player enters the gameplay scene.
2. Player progresses through the Library, Canteen, Gym, and Courtyard stages.
3. Player fights standard infected enemies in the active stage.
4. Player reaches the stage boss encounter.
5. Player views the ending.

## UC-04 Explore Campus

**Primary Actor:** Player  
**Goal:** Move through the infected school areas  
**Precondition:** Gameplay has started  
**Postcondition:** Player advances to the next stage or encounter

**Basic Flow**

1. Player moves through the current area.
2. System presents one of the story stages: Library, Canteen, Gym, or Courtyard.
3. Player clears the current stage and progresses forward.

## UC-05 Fight Minions

**Primary Actor:** Player  
**Goal:** Defeat infected enemies blocking progression  
**Precondition:** Player is inside gameplay  
**Postcondition:** Stage is cleared or the player is defeated

**Basic Flow**

1. System spawns stage-specific enemies such as infected librarians, students, or janitors.
2. Player attacks enemies using the assigned weapon.
3. Player may use a character skill.
4. Player may reload the weapon.
5. System removes defeated enemies.

## UC-06 Fight Boss

**Primary Actor:** Player  
**Goal:** Defeat a boss encounter tied to the current stage  
**Precondition:** Player reached a boss stage  
**Postcondition:** Boss is defeated or player is defeated

**Specializations**

- `Fight Security Guard`
- `Fight Mutated Vendor`
- `Fight Caesar Hunos`
- `Fight LAIR Mimic`

## UC-07 Use Character Skill

**Primary Actor:** Player  
**Goal:** Trigger the selected survivor's special ability during combat  
**Precondition:** Gameplay is active and the skill is not on cooldown  
**Postcondition:** The skill effect is applied and the cooldown starts

**Basic Flow**

1. Player presses the skill input during gameplay.
2. System checks whether the skill is available.
3. System applies the selected character's skill effect.
4. System starts the skill cooldown.

## UC-08 Reload Weapon

**Primary Actor:** Player  
**Goal:** Refill the current weapon magazine  
**Precondition:** Gameplay is active and the weapon is not already full  
**Postcondition:** The magazine is refilled after the reload delay

**Basic Flow**

1. Player presses the reload input during gameplay.
2. System checks whether reload is allowed.
3. System starts the reload timer.
4. System refills the weapon magazine when the reload completes.

## UC-09 View Ending

**Primary Actor:** Player  
**Goal:** See the outcome of the game run  
**Precondition:** Final sequence is reached  
**Postcondition:** Player may return to selection or exit

**Basic Flow**

1. System shows the ending overlay.
2. Player reads the result of the run.
3. Player decides whether to return or exit.

## UC-10 Exit Game

**Primary Actor:** Player  
**Goal:** Leave the application  
**Precondition:** Application is open  
**Postcondition:** Session ends
