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
3. Player fights minions and bosses inside the active stage.
5. Player views the ending.

## UC-04 Clear Story Stages

**Primary Actor:** Player  
**Goal:** Complete the required school stages and advance the story  
**Precondition:** Gameplay has started  
**Postcondition:** Player clears the current stage and unlocks the next one

**Basic Flow**

1. Player moves through the current area.
2. System presents one of the story stages: Library, Canteen, Gym, or Courtyard.
3. Player defeats the stage minions and boss.
4. System opens the exit to the next stage.

## UC-05 Use Character Skill

**Primary Actor:** Player  
**Goal:** Trigger the selected survivor's special ability during combat  
**Precondition:** Gameplay is active and the skill is not on cooldown  
**Postcondition:** The skill effect is applied and the cooldown starts

**Basic Flow**

1. Player presses the skill input during gameplay.
2. System checks whether the skill is available.
3. System applies the selected character's skill effect.
4. System starts the skill cooldown.

## UC-06 Reload Weapon

**Primary Actor:** Player  
**Goal:** Refill the current weapon magazine  
**Precondition:** Gameplay is active and the weapon is not already full  
**Postcondition:** The magazine is refilled after the reload delay

**Basic Flow**

1. Player presses the reload input during gameplay.
2. System checks whether reload is allowed.
3. System starts the reload timer.
4. System refills the weapon magazine when the reload completes.

## UC-07 View Ending

**Primary Actor:** Player  
**Goal:** See the outcome of the game run  
**Precondition:** Final sequence is reached  
**Postcondition:** Player may return to selection or exit

**Basic Flow**

1. System shows the ending overlay.
2. Player reads the result of the run.
3. Player decides whether to return or exit.

## UC-08 Exit Game

**Primary Actor:** Player  
**Goal:** Leave the application  
**Precondition:** Application is open  
**Postcondition:** Session ends
