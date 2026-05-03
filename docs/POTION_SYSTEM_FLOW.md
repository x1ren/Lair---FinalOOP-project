# Potion System Flow Diagram

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        POTION SYSTEM                             │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│   Potion     │      │  GameScene   │      │ GameHudRen-  │
│   (Class)    │◄────►│   (Logic)    │◄────►│   derer      │
└──────────────┘      └──────────────┘      └──────────────┘
      │                      │                      │
      │                      │                      │
   Rendering            Game Loop              UI Display
   Animation            Collision               HUD Panel
   Visual FX            Inventory              Status Text
```

## Potion Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│                     POTION LIFECYCLE                             │
└─────────────────────────────────────────────────────────────────┘

1. ENEMY DEFEAT
   ┌──────────────┐
   │ Enemy Dies   │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Random Check │ ──► 10% chance
   └──────┬───────┘
          │ YES
          ▼
   ┌──────────────┐
   │ Spawn Potion │
   └──────┬───────┘
          │
          ▼

2. POTION ACTIVE
   ┌──────────────┐
   │ Float & Glow │ ──► Continuous animation
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Wait for     │
   │ Player       │
   └──────┬───────┘
          │
          ▼

3. COLLECTION
   ┌──────────────┐
   │ Player Touch │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Add to       │
   │ Inventory    │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Update HUD   │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Show Status  │
   └──────────────┘

4. USAGE
   ┌──────────────┐
   │ Press '4'    │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Check Count  │ ──► If 0: Show "No potions"
   └──────┬───────┘
          │ > 0
          ▼
   ┌──────────────┐
   │ Check HP     │ ──► If Full: Show "Already full"
   └──────┬───────┘
          │ < Max
          ▼
   ┌──────────────┐
   │ Heal 15%     │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Decrease     │
   │ Count        │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Update HUD   │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ Show Status  │
   └──────────────┘
```

## Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                        DATA FLOW                                 │
└─────────────────────────────────────────────────────────────────┘

INPUT LAYER
   ┌──────────────┐
   │ Keyboard '4' │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ InputHandler │
   └──────┬───────┘
          │
          ▼

LOGIC LAYER
   ┌──────────────┐
   │ GameScene    │
   │ usePotion()  │
   └──────┬───────┘
          │
          ├──► Check potionCount
          │
          ├──► Check hp vs maxHp
          │
          ├──► Calculate heal (maxHp * 0.15)
          │
          ├──► Update hp
          │
          └──► Update potionCount
          │
          ▼

RENDER LAYER
   ┌──────────────┐
   │ GameHudRen-  │
   │ derer        │
   └──────┬───────┘
          │
          ├──► Draw panel
          │
          ├──► Draw "POTION [4]"
          │
          └──► Draw "x{count}"
          │
          ▼

OUTPUT LAYER
   ┌──────────────┐
   │ Screen       │
   │ Display      │
   └──────────────┘
```

## Stage Persistence

```
┌─────────────────────────────────────────────────────────────────┐
│                   STAGE PERSISTENCE                              │
└─────────────────────────────────────────────────────────────────┘

STAGE 1
   ┌──────────────┐
   │ Start: 0     │
   │ Collected: 3 │
   │ Used: 1      │
   │ End: 2       │ ──────┐
   └──────────────┘       │
                          │
                          │ Persist
                          │
STAGE 2                   │
   ┌──────────────┐       │
   │ Start: 2     │◄──────┘
   │ Collected: 2 │
   │ Used: 0      │
   │ End: 4       │ ──────┐
   └──────────────┘       │
                          │
                          │ Persist
                          │
STAGE 3                   │
   ┌──────────────┐       │
   │ Start: 4     │◄──────┘
   │ Collected: 1 │
   │ Used: 2      │
   │ End: 3       │
   └──────────────┘

KEY POINT: potionCount is NEVER reset between stages!
```

## Collision Detection

```
┌─────────────────────────────────────────────────────────────────┐
│                  COLLISION DETECTION                             │
└─────────────────────────────────────────────────────────────────┘

Every Frame:
   ┌──────────────┐
   │ updatePotions│
   │ (dt)         │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ For each     │
   │ potion       │
   └──────┬───────┘
          │
          ├──► Update animation
          │
          ▼
   ┌──────────────┐
   │ Check        │
   │ intersects   │
   │ with player  │
   └──────┬───────┘
          │
          ▼
   ┌──────────────┐
   │ If YES:      │
   │ - Add to inv │
   │ - Remove     │
   │ - Show msg   │
   └──────────────┘

Collision Box:
   Player: 42×58 pixels
   Potion: 24×24 pixels
   
   ┌────────────┐
   │   Player   │
   │   ┌──┐     │
   │   │P │     │  ◄── If boxes overlap
   │   └──┘     │      pickup occurs
   └────────────┘
```

## Drop Rate Probability

```
┌─────────────────────────────────────────────────────────────────┐
│                    DROP RATE ANALYSIS                            │
└─────────────────────────────────────────────────────────────────┘

10% Drop Rate = 0.10 probability

Expected Drops:
   10 enemies  → ~1 potion
   50 enemies  → ~5 potions
   100 enemies → ~10 potions

Probability Distribution:
   
   Enemies │ 0 drops │ 1 drop │ 2 drops │ 3+ drops
   ────────┼─────────┼────────┼─────────┼──────────
   10      │  34.9%  │ 38.7%  │  19.4%  │   7.0%
   20      │  12.2%  │ 27.0%  │  28.5%  │  32.3%
   50      │   0.5%  │  3.4%  │  10.2%  │  86.0%

Random Check:
   random.nextDouble() < 0.10
   
   Returns: 0.0 to 1.0
   If < 0.10 → DROP
   If ≥ 0.10 → NO DROP
```

## HUD Layout

```
┌─────────────────────────────────────────────────────────────────┐
│                      HUD LAYOUT                                  │
└─────────────────────────────────────────────────────────────────┘

Top-Right Panel (204×136 pixels):

   ┌────────────────────┐
   │ AMMO               │ ← Line 1 (Y: 28)
   │ 25/30              │ ← Line 2 (Y: 48)
   │                    │
   │ RELOAD             │ ← Line 3 (Y: 72)
   │ READY              │ ← Line 4 (Y: 92)
   │                    │
   │ POTION  [4]        │ ← Line 5 (Y: 116) GREEN
   │ x3                 │ ← Line 6 (Y: 136) WHITE
   └────────────────────┘

Position: (viewportWidth - 224, 18)
Background: Dark semi-transparent
Border: Green accent
```

## Animation Timeline

```
┌─────────────────────────────────────────────────────────────────┐
│                   ANIMATION TIMELINE                             │
└─────────────────────────────────────────────────────────────────┘

Time (seconds) │ Float Y │ Glow Alpha │ Sparkle Alpha
───────────────┼─────────┼────────────┼──────────────
0.00           │   0.0   │    0.30    │     0.50
0.25           │  +4.0   │    0.40    │     0.75
0.50           │  +8.0   │    0.50    │     1.00
0.75           │  +4.0   │    0.40    │     0.75
1.00           │   0.0   │    0.30    │     0.50
1.25           │  -4.0   │    0.40    │     0.75
1.50           │  -8.0   │    0.50    │     1.00
1.75           │  -4.0   │    0.40    │     0.75
2.00           │   0.0   │    0.30    │     0.50
(repeats)

Float:   sin(time * 3.0) * 8.0
Glow:    0.3 + 0.2 * sin(time * 4.0)
Sparkle: 0.5 + 0.5 * sin(time * 6.0)
```

## Error Handling

```
┌─────────────────────────────────────────────────────────────────┐
│                    ERROR HANDLING                                │
└─────────────────────────────────────────────────────────────────┘

usePotion() Decision Tree:

   Press '4'
      │
      ▼
   potionCount > 0?
      │
      ├─NO──► Show "No potions available."
      │       Return
      │
      ├─YES─► hp < maxHp?
              │
              ├─NO──► Show "Health is already full."
              │       Return
              │
              └─YES─► Heal player
                      Decrease count
                      Show success message
                      Return
```

## Integration Points

```
┌─────────────────────────────────────────────────────────────────┐
│                  INTEGRATION POINTS                              │
└─────────────────────────────────────────────────────────────────┘

1. INPUT SYSTEM
   InputHandler.isJustPressed(KeyCode.DIGIT4)
   ↓
   GameScene.handlePlayerInput()
   ↓
   GameScene.usePotion()

2. COLLISION SYSTEM
   CollisionManager.intersects(player, potion)
   ↓
   GameScene.updatePotions()
   ↓
   potionCount++

3. ENEMY SYSTEM
   EnemyActor.isDefeated()
   ↓
   GameScene.updateProjectiles()
   ↓
   random.nextDouble() < 0.10
   ↓
   new Potion(x, y)

4. RENDER SYSTEM
   GameScene.renderGame()
   ↓
   potions.renderAll(gc)
   ↓
   Potion.render(gc)

5. HUD SYSTEM
   GameScene.renderGame()
   ↓
   hudRenderer.renderHud(..., potionCount, ...)
   ↓
   GameHudRenderer.renderHud()
```
