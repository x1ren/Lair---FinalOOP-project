# Potion System Documentation

## Overview
The potion system adds a health restoration mechanic to the game, allowing players to recover health during combat by collecting and using potions.

## Features

### 1. Potion Drops
- **Drop Rate**: 10% chance when any enemy is defeated
- **Drop Location**: Potions spawn at the enemy's center position
- **Visual**: Green glowing potion bottle with floating animation

### 2. Potion Collection
- **Automatic Pickup**: Walk over a potion to collect it
- **Feedback**: Status message shows current potion count
- **Persistence**: Potions remain in inventory across all stages

### 3. Potion Usage
- **Activation**: Press the '4' key (works with both main keyboard and numpad)
- **Effect**: Restores 15% of maximum health
- **Restrictions**: 
  - Cannot use if no potions in inventory
  - Cannot use if health is already full
- **Feedback**: Status message shows heal amount and remaining potions

### 4. UI Display
- **Location**: Right panel of HUD (below ammo/reload info)
- **Display**: Shows "POTION [4]" label and "x{count}" counter
- **Color**: Green text to match potion theme
- **Controls Hint**: Updated to include "4 potion" in bottom control bar

## Implementation Details

### Classes Modified

#### 1. `Potion.java` (New)
- Extends `GameObject`
- Implements floating animation (sine wave)
- Implements pulsing glow effect
- Renders procedural potion sprite with:
  - Outer glow (pulsing green)
  - Bottle body (dark green glass)
  - Cork/cap (brown)
  - Liquid (bright green)
  - Highlight shine (white)
  - Sparkle effect (pulsing white)

#### 2. `GameScene.java`
- Added `EntityManager<Potion> potions` field
- Added `int potionCount` field
- Added `updatePotions(double dt)` method for pickup detection
- Added `usePotion()` method for consumption logic
- Modified `updateProjectiles()` to drop potions on enemy defeat (10% chance)
- Added potion rendering in `renderGame()`
- Added '4' key input handling in `handlePlayerInput()`
- Potions persist across stages (not cleared in `startStage()`)

#### 3. `GameHudRenderer.java`
- Added `potionCount` parameter to `renderHud()` method
- Expanded right panel height from 104 to 136 pixels
- Added potion display section showing:
  - "POTION [4]" label in green
  - "x{count}" counter in white
- Updated controls hint to include "4 potion"

## Game Balance

### Health Restoration
- **Heal Amount**: 15% of max HP
- **Example**: If max HP is 100, each potion restores 15 HP
- **Scaling**: Heal amount scales with character's max HP

### Drop Rate Tuning
- **Current**: 10% drop rate
- **Rationale**: Provides occasional health recovery without making the game too easy
- **Adjustable**: Can be modified in `GameScene.updateProjectiles()` method

### Strategic Considerations
- Players must decide when to use potions (save for boss fights vs. use immediately)
- Potions persist across stages, encouraging resource management
- Limited by drop rate, preventing infinite healing

## Testing Checklist

- [x] Potions drop from defeated enemies at 10% rate
- [x] Potions can be picked up by walking over them
- [x] Potion count displays correctly in HUD
- [x] Pressing '4' uses a potion and restores 15% HP
- [x] Cannot use potion when inventory is empty
- [x] Cannot use potion when health is full
- [x] Potions persist across stage transitions
- [x] Status messages display correctly
- [x] Potion visual renders with animations
- [x] Controls hint updated with potion key

## Future Enhancements (Optional)

1. **Different Potion Types**
   - Mana potions for ability cooldown reduction
   - Speed potions for temporary movement boost
   - Damage potions for temporary attack boost

2. **Potion Rarity**
   - Common (15% heal)
   - Rare (30% heal)
   - Epic (50% heal + buff)

3. **Potion Crafting**
   - Collect ingredients from enemies
   - Craft potions at safe zones

4. **Maximum Potion Capacity**
   - Limit inventory to 5-10 potions
   - Encourages strategic usage

5. **Potion Shop**
   - Purchase potions with currency
   - Unlocks between stages
