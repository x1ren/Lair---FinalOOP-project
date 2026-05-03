# Potion System Visual Reference

## In-Game Potion Appearance

### Potion Sprite (24x24 pixels)
```
    ████████        <- Cork/Cap (brown)
  ██████████████    
 ████░░░░░░░░████   <- Bottle outline (dark green)
 ██░░████████░░██   
 ██░░██████░░░░██   <- Liquid (bright green)
 ██░░██████░░░░██      with white highlight
 ██░░░░░░░░░░░░██   
  ██████████████    
    ████████        
```

### Animation Effects

1. **Floating Animation**
   - Moves up and down in a sine wave pattern
   - Amplitude: 8 pixels
   - Speed: 3 units per second
   - Creates a gentle bobbing effect

2. **Glow Effect**
   - Pulsing green aura around the potion
   - Extends 6 pixels beyond bottle edges
   - Alpha varies from 0.3 to 0.5
   - Pulse speed: 4 units per second

3. **Sparkle Effect**
   - Small white pixel on bottle surface
   - Pulsing alpha from 0.5 to 1.0
   - Faster pulse than glow (1.5x speed)

## HUD Display

### Right Panel Layout
```
┌─────────────────────────┐
│ AMMO                    │
│ 25/30                   │
│                         │
│ RELOAD                  │
│ READY                   │
│                         │
│ POTION  [4]            │  <- New section (green text)
│ x3                     │  <- Potion count (white text)
└─────────────────────────┘
```

### Panel Dimensions
- Width: 204 pixels
- Height: 136 pixels (expanded from 104)
- Position: Top-right corner (20 pixels from edges)
- Background: Dark semi-transparent
- Border: Green accent color

## Status Messages

### Collection Message
```
┌──────────────────────────────────────────────┐
│  Potion collected! Press 4 to use. (3 total) │
└──────────────────────────────────────────────┘
```

### Usage Message (Success)
```
┌──────────────────────────────────────────────┐
│  Potion used! Restored 15 HP. (2 remaining)  │
└──────────────────────────────────────────────┘
```

### Usage Message (No Potions)
```
┌──────────────────────────────────────────────┐
│  No potions available.                        │
└──────────────────────────────────────────────┘
```

### Usage Message (Full Health)
```
┌──────────────────────────────────────────────┐
│  Health is already full.                      │
└──────────────────────────────────────────────┘
```

## Color Scheme

### Potion Colors
- **Outer Glow**: RGB(51, 230, 77) with pulsing alpha
- **Bottle Glass**: RGB(26, 102, 51) at 90% opacity
- **Cork/Cap**: RGB(153, 102, 51)
- **Liquid**: RGB(51, 242, 77) at 85% opacity
- **Highlight**: RGB(204, 255, 230) at 60% opacity
- **Sparkle**: RGB(255, 255, 255) with pulsing alpha

### HUD Colors
- **Label Text**: RGB(51, 242, 77) - Bright green
- **Count Text**: RGB(255, 255, 255) - White
- **Panel Background**: RGB(3, 8, 13) at 84% opacity
- **Panel Border**: RGB(26, 194, 107) at 72% opacity

## Drop Behavior

### When Enemy Dies
```
Enemy Position: (X, Y)
         ↓
Potion Spawns at: (X - 12, Y + Height/2)
         ↓
Begins floating animation
         ↓
Player walks over potion
         ↓
Potion added to inventory
         ↓
Status message displayed
```

### Drop Rate Visualization
```
10 Enemies Defeated:
████████░░  (10% = 1 potion on average)

100 Enemies Defeated:
██████████  (10% = 10 potions on average)
```

## Gameplay Flow

### Typical Usage Scenario
```
1. Player fights enemies
   ↓
2. Enemy defeated (10% chance)
   ↓
3. Potion drops and floats
   ↓
4. Player collects potion
   ↓
5. Potion count increases in HUD
   ↓
6. Player takes damage in combat
   ↓
7. Player presses '4' key
   ↓
8. Health restored by 15%
   ↓
9. Potion count decreases
   ↓
10. Status message confirms usage
```

## Stage Persistence

### Stage Transition
```
Stage 1: Collected 3 potions
         Used 1 potion
         Remaining: 2 potions
         ↓
Stage 2: Start with 2 potions
         Collected 2 more potions
         Remaining: 4 potions
         ↓
Stage 3: Start with 4 potions
         (Potions persist!)
```

## Controls Reference

### Updated Control Bar
```
Controls: WASD/arrows  |  LMB shoot  |  Q skill  |  E dash  |  R reload  |  4 potion  |  Space jump  |  Esc pause
                                                                           ^^^^^^^^^^
                                                                           New control
```

### Key Bindings
- **Main Keyboard**: Press '4' above QWERTY row
- **Numpad**: Press '4' on numeric keypad
- Both keys work identically
