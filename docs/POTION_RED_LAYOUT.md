# Red Potion - New Layout Reference

## Updated Visual Design

### Red Potion Sprite (24x24 pixels)
```
    ████████        <- Cork/Cap (brown)
  ██████████████    
 ████▓▓▓▓▓▓▓▓████   <- Bottle outline (dark red)
 ██▓▓████████▓▓██   
 ██▓▓██████▓▓▓▓██   <- Liquid (bright red)
 ██▓▓██████▓▓▓▓██      with white highlight
 ██▓▓▓▓▓▓▓▓▓▓▓▓██   
  ██████████████    
    ████████        

Legend:
█ = Dark red glass
▓ = Bright red liquid
```

### Animation Effects (Unchanged)
- **Floating**: Sine wave, 8px amplitude
- **Glow**: Pulsing red aura (alpha 0.3-0.5)
- **Sparkle**: White pixel (alpha 0.5-1.0)

## New HUD Layout - Side by Side

### Full HUD Overview
```
┌──────────────────────────────────────────────────────────────────┐
│                         TOP OF SCREEN                             │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  ┌─────────────────┐    ┌──────────────────┐    ┌──────┬──────┐ │
│  │ CHARACTER INFO  │    │  STAGE INFO      │    │ AMMO │POTION│ │
│  │ Name            │    │  Stage Name      │    │      │      │ │
│  │ HP: ████████    │    │  Objective       │    │25/30 │ x3   │ │
│  │ Skill: [Q]      │    └──────────────────┘    │      │      │ │
│  │ Ability: READY  │                            │RELOAD│      │ │
│  └─────────────────┘                            │READY │      │ │
│                                                  └──────┴──────┘ │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### Right Panel Detail - Side by Side
```
┌────────────────────────────────────────┐
│         TOP-RIGHT CORNER               │
├────────────────────────────────────────┤
│                                        │
│  ┌──────────────┐  ┌─────────────┐   │
│  │ AMMO         │  │ POTION  [4] │   │
│  │ 25/30        │  │ x3          │   │
│  │              │  │             │   │
│  │ RELOAD       │  │             │   │
│  │ READY        │  │             │   │
│  └──────────────┘  └─────────────┘   │
│   Green border      Red border       │
│   204×104 px        194×104 px       │
│                                        │
└────────────────────────────────────────┘
```

### Panel Positioning
```
Screen Width: 1280px
Panel Heights: 104px each

Ammo Panel:
  X: viewportWidth - 428 = 852px
  Y: 18px
  Width: 204px
  Height: 104px
  Border: Green (0.10, 0.76, 0.42)

Potion Panel:
  X: viewportWidth - 214 = 1066px
  Y: 18px
  Width: 194px
  Height: 104px
  Border: Red (0.95, 0.2, 0.2)

Gap between panels: 10px
```

## Color Specifications

### Red Potion Colors (RGB 0-1 scale)

| Element | Color | RGB Values | Alpha | Hex |
|---------|-------|------------|-------|-----|
| Outer Glow | Bright Red | 0.95, 0.2, 0.2 | 0.3-0.5 | #F23333 |
| Bottle Glass | Dark Red | 0.5, 0.1, 0.1 | 0.9 | #801A1A |
| Cork/Cap | Brown | 0.6, 0.4, 0.2 | 1.0 | #996633 |
| Liquid | Bright Red | 0.95, 0.2, 0.2 | 0.85 | #F23333 |
| Highlight | Light Red | 1.0, 0.8, 0.8 | 0.6 | #FFCCCC |
| Sparkle | White | 1.0, 1.0, 1.0 | 0.5-1.0 | #FFFFFF |

### UI Text Colors

| Element | Color | RGB Values | Font Size | Weight |
|---------|-------|------------|-----------|--------|
| "POTION [4]" | Red | 0.95, 0.3, 0.3 | 12pt | Bold |
| "x{count}" | White | 1.0, 1.0, 1.0 | 18pt | Bold |
| Panel Border | Red | 0.95, 0.2, 0.2 | - | - |
| Panel BG | Dark | 0.01, 0.03, 0.05 | - | - |

## Text Layout in Potion Panel

```
┌─────────────────┐
│ POTION  [4]     │ ← Y: 28px, Size: 12pt, Red
│                 │
│                 │
│ x3              │ ← Y: 58px, Size: 18pt, White
│                 │
│                 │
└─────────────────┘
```

### Text Positioning
- **Label**: X: viewportWidth - 194, Y: panelY + 28
- **Count**: X: viewportWidth - 194, Y: panelY + 58
- **Alignment**: Left-aligned within panel
- **Padding**: 10px from panel edge

## Comparison: Green vs Red

### Visual Impact
```
GREEN POTION:              RED POTION:
🟢 Glow                    🔴 Glow
🟢 Liquid                  🔴 Liquid
✓ Nature/mana theme        ✓ Health/vitality theme
✓ Less common              ✓ Universal health symbol
✓ Subtle                   ✓ High visibility
```

### Psychological Association
- **Green**: Nature, poison, mana, energy
- **Red**: Health, blood, vitality, urgency ✅

### Game Design Benefits
✅ Matches health bar color (red)  
✅ Instantly recognizable as health potion  
✅ Better contrast on most backgrounds  
✅ Industry standard (most games use red for health)  

## Layout Comparison: Vertical vs Side-by-Side

### Before (Vertical Stack)
```
┌─────────────────┐
│ AMMO            │
│ 25/30           │
│                 │
│ RELOAD          │
│ READY           │
│                 │
│ POTION  [4]     │
│ x3              │
└─────────────────┘
Height: 136px
Width: 204px
```

### After (Side-by-Side)
```
┌──────────┐ ┌──────────┐
│ AMMO     │ │ POTION   │
│ 25/30    │ │ [4]      │
│          │ │          │
│ RELOAD   │ │ x3       │
│ READY    │ │          │
└──────────┘ └──────────┘
Height: 104px each
Total Width: 408px
```

### Advantages of Side-by-Side
✅ **Saves vertical space** - 104px vs 136px  
✅ **Better visual balance** - symmetric layout  
✅ **Clearer separation** - distinct panels with colored borders  
✅ **Easier scanning** - horizontal eye movement is faster  
✅ **More professional** - modern UI design pattern  
✅ **Color coding** - green for ammo, red for health  

## In-Game Appearance

### Potion Drop
```
Enemy defeated → 10% chance
         ↓
    🔴 Red potion spawns
         ↓
    Floats with red glow
         ↓
    Player walks over
         ↓
    "Potion collected! (x3)"
```

### HUD Update
```
Before pickup:          After pickup:
┌──────────┐           ┌──────────┐
│ POTION   │           │ POTION   │
│ [4]      │           │ [4]      │
│          │           │          │
│ x2       │           │ x3       │ ← Increased
└──────────┘           └──────────┘
```

### Usage Flow
```
Press '4' key
     ↓
┌──────────┐           ┌──────────┐
│ POTION   │           │ POTION   │
│ [4]      │           │ [4]      │
│          │           │          │
│ x3       │    →      │ x2       │ ← Decreased
└──────────┘           └──────────┘
     ↓
HP restored +15%
     ↓
"Potion used! Restored 15 HP. (2 remaining)"
```

## Controls Reference

### Updated Control Bar
```
Controls: WASD/arrows  |  LMB shoot  |  Q skill  |  E dash  |  4 potion  |  R reload  |  Space jump  |  Esc pause
                                                              ^^^^^^^^^^
                                                              Moved earlier
```

### Key Binding
- **Main Keyboard**: '4' key above QWERTY
- **Numpad**: '4' key on numeric keypad
- **Both work identically**

## Status Messages

### Collection
```
┌────────────────────────────────────────────┐
│ Potion collected! Press 4 to use. (3 total)│
└────────────────────────────────────────────┘
```

### Usage Success
```
┌────────────────────────────────────────────┐
│ Potion used! Restored 15 HP. (2 remaining) │
└────────────────────────────────────────────┘
```

### No Potions
```
┌────────────────────────────────────────────┐
│ No potions available.                       │
└────────────────────────────────────────────┘
```

### Full Health
```
┌────────────────────────────────────────────┐
│ Health is already full.                     │
└────────────────────────────────────────────┘
```

## Technical Implementation

### Color Values in Code
```java
// Potion.java - Red colors
gc.setFill(Color.color(0.95, 0.2, 0.2, glowAlpha));  // Glow
gc.setFill(Color.color(0.5, 0.1, 0.1, 0.9));         // Glass
gc.setFill(Color.color(0.95, 0.2, 0.2, 0.85));       // Liquid
gc.setFill(Color.color(1.0, 0.8, 0.8, 0.6));         // Highlight
```

### Panel Layout in Code
```java
// GameHudRenderer.java - Side-by-side panels
drawPixelPanel(viewportWidth - 428, panelY, 204, 104, ...);  // Ammo
drawPixelPanel(viewportWidth - 214, panelY, 194, 104, ...);  // Potion
```

### Text Positioning in Code
```java
// Ammo text
gc.fillText("AMMO", viewportWidth - 408, panelY + 28);

// Potion text
gc.fillText("POTION  [4]", viewportWidth - 194, panelY + 28);
gc.fillText("x" + potionCount, viewportWidth - 194, panelY + 58);
```
