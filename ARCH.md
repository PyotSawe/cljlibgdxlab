# 🎮 Game Architecture Documentation

```
    ╔════════════════════════════════════════════════════════════════╗
    ║             🌧️ Enhanced Drop Game Architecture 🪣              ║
    ║                                                                ║
    ║  A Functional-Reactive Architecture using LibGDX + Clojure    ║
    ╚════════════════════════════════════════════════════════════════╝
```

## 🏗️ System Overview

The Enhanced Drop Game employs a **layered functional-reactive architecture** that separates concerns while maintaining high performance and code clarity.

```
                    ╔═══════════════════════════════════════╗
                    ║           PLAYER INTERACTION          ║
                    ║    🖱️ Mouse  ⌨️ Keyboard  🎮 Touch    ║
                    ╚═══════════════════════════════════════╝
                                       │
                                       ▼
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                          🎯 INPUT LAYER                               ║
    ║  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐            ║
    ║  │ Keyboard      │  │ Mouse/Touch   │  │ Game Events   │            ║
    ║  │ • WASD Keys   │  │ • Click Move  │  │ • ESC Quit    │            ║
    ║  │ • Arrow Keys  │  │ • Drag Bucket │  │ • Pause/Play  │            ║
    ║  └───────────────┘  └───────────────┘  └───────────────┘            ║
    ╚═══════════════════════════════════════════════════════════════════════╝
                                       │
                                       ▼
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                        🧠 GAME LOGIC LAYER                           ║
    ║                                                                       ║
    ║  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐    ║
    ║  │   Physics   │ │ Collision   │ │   Scoring   │ │    Timer    │    ║
    ║  │   Engine    │ │  Detection  │ │   System    │ │   System    │    ║
    ║  │             │ │             │ │             │ │             │    ║
    ║  │ • Gravity   │ │ • AABB Algo │ │ • Combos    │ │ • Game Time │    ║
    ║  │ • Movement  │ │ • Rectangle │ │ • Multi-    │ │ • Drop Rate │    ║
    ║  │ • Velocity  │ │   Overlap   │ │   pliers    │ │ • Spawning  │    ║
    ║  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘    ║
    ║                                                                       ║
    ║  ┌─────────────────────────────────────────────────────────────┐     ║
    ║  │                    Entity Management                        │     ║
    ║  │                                                             │     ║
    ║  │  🪣 Bucket Entity        🌧️ Droplet Entities               │     ║
    ║  │  ├─ Transform           ├─ Transform Pool                  │     ║
    ║  │  ├─ Input Handler       ├─ Physics Array                  │     ║
    ║  │  ├─ Collision Box       ├─ Collision Array                │     ║
    ║  │  └─ Sprite Renderer     └─ Sprite Batch                   │     ║
    ║  └─────────────────────────────────────────────────────────────┘     ║
    ╚═══════════════════════════════════════════════════════════════════════╝
                                       │
                                       ▼
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                        💾 DATA MANAGEMENT LAYER                      ║
    ║                                                                       ║
    ║        ┌──────────────────────────────────────────────────┐          ║
    ║        │            🔄 Clojure Atoms (Thread-Safe)        │          ║
    ║        │                                                  │          ║
    ║        │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ │          ║
    ║        │  │  Score  │ │  Lives  │ │ Combos  │ │  Time   │ │          ║
    ║        │  │   📊    │ │   ❤️    │ │   🔥    │ │   ⏱️    │ │          ║
    ║        │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ │          ║
    ║        │                                                  │          ║
    ║        │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ │          ║
    ║        │  │ Bucket  │ │Droplets │ │ Assets  │ │ Config  │ │          ║
    ║        │  │ Sprite  │ │  Array  │ │ Cache   │ │  Data   │ │          ║
    ║        │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ │          ║
    ║        └──────────────────────────────────────────────────┘          ║
    ╚═══════════════════════════════════════════════════════════════════════╝
                                       │
                                       ▼
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                      🎨 PRESENTATION LAYER                           ║
    ║                                                                       ║
    ║  ┌───────────────────────────────────────────────────────────────┐    ║
    ║  │                   📷 Dual Viewport System                    │    ║
    ║  │                                                               │    ║
    ║  │  ┌─────────────────────┐   ┌─────────────────────┐          │    ║
    ║  │  │   🌍 Game World     │   │     📱 UI Overlay    │          │    ║
    ║  │  │   (FitViewport)     │   │   (ScreenViewport)   │          │    ║
    ║  │  │                     │   │                     │          │    ║
    ║  │  │ • Bucket Sprite     │   │ • Score Display     │          │    ║
    ║  │  │ • Droplet Sprites   │   │ • Combo Counter     │          │    ║
    ║  │  │ • Background        │   │ • Stats Panel       │          │    ║
    ║  │  │ • World Coords      │   │ • Controls Help     │          │    ║
    ║  │  └─────────────────────┘   └─────────────────────┘          │    ║
    ║  └───────────────────────────────────────────────────────────────┘    ║
    ║                                                                       ║
    ║  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐    ║
    ║  │ SpriteBatch │ │  Font Sys   │ │ Asset Mgmt  │ │ Color Mgmt  │    ║
    ║  │             │ │             │ │             │ │             │    ║
    ║  │ • Efficient │ │ • Multi     │ │ • Textures  │ │ • Dynamic   │    ║
    ║  │   Batching  │ │   Fonts     │ │ • Audio     │ │   Colors    │    ║
    ║  │ • GPU Opt   │ │ • Color     │ │ • Lifecycle │ │ • Feedback  │    ║
    ║  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘    ║
    ╚═══════════════════════════════════════════════════════════════════════╝
                                       │
                                       ▼
                    ╔═══════════════════════════════════════╗
                    ║         🖥️ DISPLAY OUTPUT            ║
                    ║   📺 Screen  🔊 Audio  🎵 Music      ║
                    ╚═══════════════════════════════════════╝
```

## 🔄 Game Loop Architecture

```
                         ╔═════════════════════════════════════╗
                         ║         🎯 MAIN GAME LOOP          ║
                         ║                                     ║
                         ║    (60 FPS Target / VSync)         ║
                         ╚═════════════════════════════════════╝
                                        │
                                        ▼
        ┌─────────────────────────────────────────────────────────────┐
        │                    ⏱️ FRAME START                           │
        │                (Delta Time Calculation)                    │
        └─────────────────────────────────────────────────────────────┘
                                        │
                                        ▼
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                          📥 INPUT PROCESSING                          ║
    ║                                                                       ║
    ║    ┌─────────────┐      ┌─────────────┐      ┌─────────────┐        ║
    ║    │  Keyboard   │ ──▶  │   Mouse     │ ──▶  │   Events    │        ║
    ║    │   Polling   │      │  Tracking   │      │ Processing  │        ║
    ║    └─────────────┘      └─────────────┘      └─────────────┘        ║
    ║           │                     │                     │              ║
    ║           └─────────────────────┼─────────────────────┘              ║
    ╚═══════════════════════════════════════════════════════════════════════╝
                                        │
                                        ▼
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                         🧮 LOGIC UPDATE                              ║
    ║                                                                       ║
    ║  Step 1: Update Game Timer    ┌─────────────────────────────────────┐ ║
    ║  ┌─────────────────────┐      │         Physics Simulation          │ ║
    ║  │   game-time += Δt   │      │                                     │ ║
    ║  │   combo-timer -= Δt │ ──▶  │  ┌─────────┐    ┌─────────┐        │ ║
    ║  │   drop-timer += Δt  │      │  │ Bucket  │    │Droplet  │        │ ║
    ║  └─────────────────────┘      │  │Movement │    │Falling  │        │ ║
    ║                               │  │         │    │        │        │ ║
    ║  Step 2: Entity Updates       │  │ Pos +=  │    │Pos -=  │        │ ║
    ║  ┌─────────────────────┐      │  │Vel*Δt   │    │2*Δt    │        │ ║
    ║  │ Spawn New Droplets  │      │  └─────────┘    └─────────┘        │ ║
    ║  │ (if timer > 1.0s)   │ ──▶  │                                     │ ║
    ║  │ Remove Old Droplets │      │  ┌─────────────────────────────────┐ │ ║
    ║  └─────────────────────┘      │  │      Collision Detection        │ ║
    ║                               │  │                                 │ ║
    ║  Step 3: Collision Check      │  │  for each droplet:             │ ║
    ║  ┌─────────────────────┐      │  │    if overlaps(bucket, drop):   │ ║
    ║  │ AABB Algorithm      │      │  │      ├─ remove droplet         │ ║
    ║  │ O(n) complexity     │ ──▶  │  │      ├─ play sound            │ ║
    ║  │ Rectangle overlap   │      │  │      ├─ increment score       │ ║
    ║  └─────────────────────┘      │  │      └─ update combo           │ ║
    ║                               │  └─────────────────────────────────┘ │ ║
    ║  Step 4: Game Rules           └─────────────────────────────────────┘ ║
    ║  ┌─────────────────────┐                                             ║
    ║  │ Update Score/Combo  │                                             ║
    ║  │ Check Lives/Health  │                                             ║
    ║  │ Difficulty Scaling  │                                             ║
    ║  └─────────────────────┘                                             ║
    ╚═══════════════════════════════════════════════════════════════════════╝
                                        │
                                        ▼
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                          🎨 RENDERING PIPELINE                       ║
    ║                                                                       ║
    ║   ┌─────────────────────────────────────────────────────────────────┐ ║
    ║   │                    🌍 World Rendering                           │ ║
    ║   │                                                                 │ ║
    ║   │  1. Clear Screen (Blue Background)                             │ ║
    ║   │     ScreenUtils.clear(0.1, 0.3, 0.8, 1.0)                     │ ║
    ║   │                           │                                     │ ║
    ║   │  2. Setup Game Camera     ▼                                     │ ║
    ║   │     viewport.apply()   ┌─────────────────────┐                  │ ║
    ║   │     setProjection()    │   SpriteBatch       │                  │ ║
    ║   │                       │      .begin()        │                  │ ║
    ║   │  3. Draw Game World   │                     │                  │ ║
    ║   │     ├─ Background     │  ┌─────────────────┐ │                  │ ║
    ║   │     ├─ Bucket Sprite  │  │ Background Img  │ │                  │ ║
    ║   │     └─ Droplets       │  │ Bucket Sprite   │ │                  │ ║
    ║   │                       │  │ Droplet Array   │ │                  │ ║
    ║   │                       │  └─────────────────┘ │                  │ ║
    ║   │                       │     .end()           │                  │ ║
    ║   │                       └─────────────────────┘                  │ ║
    ║   └─────────────────────────────────────────────────────────────────┘ ║
    ║                                        │                              ║
    ║                                        ▼                              ║
    ║   ┌─────────────────────────────────────────────────────────────────┐ ║
    ║   │                     📱 UI Overlay Rendering                     │ ║
    ║   │                                                                 │ ║
    ║   │  1. Switch to UI Camera                                         │ ║
    ║   │     ui-viewport.apply()                                         │ ║
    ║   │                           │                                     │ ║
    ║   │  2. Setup UI Projection   ▼                                     │ ║
    ║   │     setProjection()    ┌─────────────────────┐                  │ ║
    ║   │                       │   SpriteBatch       │                  │ ║
    ║   │  3. Draw UI Elements  │      .begin()        │                  │ ║
    ║   │                       │                     │                  │ ║
    ║   │   ┌─────────────────┐ │  ┌─────────────────┐ │                  │ ║
    ║   │   │ Title Font      │ │  │ 🌧️ GAME TITLE   │ │                  │ ║
    ║   │   │ (Yellow)        │ │  │ Score: 1240     │ │                  │ ║
    ║   │   └─────────────────┘ │  │ Lives: ❤️❤️❤️    │ │                  │ ║
    ║   │                       │  │ Combo: 🔥x8     │ │                  │ ║
    ║   │   ┌─────────────────┐ │  │ Time: 02:34     │ │                  │ ║
    ║   │   │ Score Font      │ │  │ Accuracy: 85%   │ │                  │ ║
    ║   │   │ (Cyan/Gold)     │ │  │ Controls Help   │ │                  │ ║
    ║   │   └─────────────────┘ │  └─────────────────┘ │                  │ ║
    ║   │                       │     .end()           │                  │ ║
    ║   │                       └─────────────────────┘                  │ ║
    ║   └─────────────────────────────────────────────────────────────────┘ ║
    ╚═══════════════════════════════════════════════════════════════════════╝
                                        │
                                        ▼
        ┌─────────────────────────────────────────────────────────────┐
        │                     🔄 FRAME END                           │
        │               (VSync / Frame Limiting)                     │
        └─────────────────────────────────────────────────────────────┘
```

## 🏗️ Entity-Component Architecture

```
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                        🎯 ENTITY ARCHITECTURE                         ║
    ╚═══════════════════════════════════════════════════════════════════════╝

              🪣 BUCKET ENTITY                    🌧️ DROPLET ENTITIES
    ╔═══════════════════════════════════╗  ╔═══════════════════════════════════╗
    ║                                   ║  ║                                   ║
    ║  ┌─────────────────────────────┐  ║  ║  ┌─────────────────────────────┐  ║
    ║  │     📍 Transform Component   │  ║  ║  │     📍 Transform Component   │  ║
    ║  │                             │  ║  ║  │                             │  ║
    ║  │  • Position (x, y)          │  ║  ║  │  • Position (x, y)          │  ║
    ║  │  • Size (width, height)     │  ║  ║  │  • Size (1x1 world units)   │  ║
    ║  │  • World Coordinates        │  ║  ║  │  • Velocity (0, -2*speed)   │  ║
    ║  └─────────────────────────────┘  ║  ║  └─────────────────────────────┘  ║
    ║               │                   ║  ║               │                   ║
    ║               ▼                   ║  ║               ▼                   ║
    ║  ┌─────────────────────────────┐  ║  ║  ┌─────────────────────────────┐  ║
    ║  │      🎮 Input Component      │  ║  ║  │     ⚛️ Physics Component     │  ║
    ║  │                             │  ║  ║  │                             │  ║
    ║  │  • WASD Key Handler         │  ║  ║  │  • Gravity Simulation       │  ║
    ║  │  • Arrow Key Handler        │  ║  ║  │  • Downward Movement        │  ║
    ║  │  • Mouse Click Handler      │  ║  ║  │  • Constant Velocity        │  ║
    ║  │  • Boundary Constraints     │  ║  ║  │  • Screen Edge Detection    │  ║
    ║  └─────────────────────────────┘  ║  ║  └─────────────────────────────┘  ║
    ║               │                   ║  ║               │                   ║
    ║               ▼                   ║  ║               ▼                   ║
    ║  ┌─────────────────────────────┐  ║  ║  ┌─────────────────────────────┐  ║
    ║  │    📦 Collision Component    │  ║  ║  │    📦 Collision Component    │  ║
    ║  │                             │  ║  ║  │                             │  ║
    ║  │  • Rectangle Bounds         │  ║  ║  │  • Rectangle Bounds         │  ║
    ║  │  • AABB Collision Box       │  ║  ║  │  • AABB Collision Box       │  ║
    ║  │  • Catch Detection Zone     │  ║  ║  │  • Bucket Overlap Check     │  ║
    ║  └─────────────────────────────┘  ║  ║  └─────────────────────────────┘  ║
    ║               │                   ║  ║               │                   ║
    ║               ▼                   ║  ║               ▼                   ║
    ║  ┌─────────────────────────────┐  ║  ║  ┌─────────────────────────────┐  ║
    ║  │     🎨 Render Component      │  ║  ║  │     🎨 Render Component      │  ║
    ║  │                             │  ║  ║  │                             │  ║
    ║  │  • Bucket Sprite            │  ║  ║  │  • Droplet Sprite           │  ║
    ║  │  • Texture Reference        │  ║  ║  │  • Texture Reference        │  ║
    ║  │  • Draw Order: Layer 1      │  ║  ║  │  • Draw Order: Layer 2      │  ║
    ║  └─────────────────────────────┘  ║  ║  └─────────────────────────────┘  ║
    ║                                   ║  ║                                   ║
    ╚═══════════════════════════════════╝  ╚═══════════════════════════════════╝
```

## 📊 Data Flow Architecture

```
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                         💾 STATE MANAGEMENT FLOW                     ║
    ╚═══════════════════════════════════════════════════════════════════════╝

                                 ┌─────────────────────┐
                                 │   🎮 User Input     │
                                 │                     │
                                 │ • Keyboard Events   │
                                 │ • Mouse Events      │
                                 │ • Touch Events      │
                                 └─────────────────────┘
                                           │
                                           ▼
                                 ┌─────────────────────┐
                                 │  📝 Input Validation │
                                 │                     │
                                 │ • Boundary Checks   │
                                 │ • Rate Limiting     │
                                 │ • State Guards     │
                                 └─────────────────────┘
                                           │
                                           ▼
        ╔══════════════════════════════════════════════════════════════════╗
        ║                    🔄 CLOJURE ATOM UPDATES                      ║
        ║                      (Thread-Safe State)                        ║
        ║                                                                  ║
        ║  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             ║
        ║  │   Score     │  │   Lives     │  │   Timer     │             ║
        ║  │   Atom      │  │   Atom      │  │   Atom      │             ║
        ║  │             │  │             │  │             │             ║
        ║  │ (swap! +10) │  │ (swap! dec) │  │ (reset! 0) │             ║
        ║  └─────────────┘  └─────────────┘  └─────────────┘             ║
        ║         │                │               │                      ║
        ║         ▼                ▼               ▼                      ║
        ║  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐             ║
        ║  │   Combo     │  │  Droplets   │  │   Bucket    │             ║
        ║  │   State     │  │   Array     │  │   Sprite    │             ║
        ║  │             │  │             │  │             │             ║
        ║  │ (update-    │  │ (.add .rem) │  │ (.setPos x) │             ║
        ║  │  combo!)    │  │             │  │             │             ║
        ║  └─────────────┘  └─────────────┘  └─────────────┘             ║
        ╚══════════════════════════════════════════════════════════════════╝
                                           │
                                           ▼
                              ┌─────────────────────────┐
                              │  🎯 State Observers     │
                              │                         │
                              │ • UI Update Triggers    │
                              │ • Audio Event Handlers  │
                              │ • Achievement Checks    │
                              └─────────────────────────┘
                                           │
                                           ▼
        ╔══════════════════════════════════════════════════════════════════╗
        ║                      🎨 REACTIVE RENDERING                      ║
        ║                                                                  ║
        ║  ┌─────────────────────────────────────────────────────────────┐ ║
        ║  │                  📺 Display Updates                         │ ║
        ║  │                                                             │ ║
        ║  │  Score: 1240  ──▶ Lives: ❤️❤️❤️  ──▶ Combo: 🔥x8           │ ║
        ║  │      │                    │                  │              │ ║
        ║  │      ▼                    ▼                  ▼              │ ║
        ║  │  Color: Cyan         Color: Green      Color: Orange        │ ║
        ║  │  Font: Large         Animation: Pulse  Effect: Glow         │ ║
        ║  └─────────────────────────────────────────────────────────────┘ ║
        ╚══════════════════════════════════════════════════════════════════╝
```

## 🔧 Performance Architecture

```
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                     ⚡ PERFORMANCE OPTIMIZATION                       ║
    ╚═══════════════════════════════════════════════════════════════════════╝

        ┌─────────────────────────────────────────────────────────────────┐
        │                    🧠 MEMORY MANAGEMENT                         │
        │                                                                 │
        │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐        │
        │  │   Stack     │    │    Heap     │    │   GPU Mem   │        │
        │  │  (Local)    │    │ (Objects)   │    │ (Textures)  │        │
        │  │             │    │             │    │             │        │
        │  │ • Temp Vars │    │ • Atoms     │    │ • Sprites   │        │
        │  │ • Calc Data │    │ • Arrays    │    │ • Buffers   │        │
        │  │ • Fast Acc  │    │ • Pooled    │    │ • Batches   │        │
        │  └─────────────┘    └─────────────┘    └─────────────┘        │
        └─────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
        ┌─────────────────────────────────────────────────────────────────┐
        │                  🚀 RENDERING OPTIMIZATION                      │
        │                                                                 │
        │    Draw Call Batching:                                          │
        │    ┌─────────────────────────────────────────────────────────┐  │
        │    │                                                         │  │
        │    │  Single SpriteBatch.begin()                             │  │
        │    │      ├─ Background (1 draw call)                       │  │
        │    │      ├─ Bucket Sprite (1 draw call)                    │  │
        │    │      ├─ All Droplets (1 batched call)                  │  │
        │    │      └─ UI Elements (1 batched call)                   │  │
        │    │  SpriteBatch.end()                                      │  │
        │    │                                                         │  │
        │    │  Result: ~4 draw calls vs ~100+ individual calls       │  │
        │    └─────────────────────────────────────────────────────────┘  │
        └─────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
        ┌─────────────────────────────────────────────────────────────────┐
        │                 ⚛️ PHYSICS OPTIMIZATION                          │
        │                                                                 │
        │  Collision Detection Pipeline:                                  │
        │  ┌─────────────────────────────────────────────────────────────┐│
        │  │                                                             ││
        │  │  Step 1: Broad Phase (Spatial Pruning)                     ││
        │  │    ├─ Only check droplets near bucket                      ││
        │  │    └─ Skip off-screen droplets                             ││
        │  │                        │                                   ││
        │  │  Step 2: Narrow Phase (AABB)     ▼                        ││
        │  │    ├─ Rectangle.overlaps() - O(1)                         ││
        │  │    ├─ Early exit on first collision                       ││
        │  │    └─ Remove from array efficiently                       ││
        │  │                                                             ││
        │  │  Complexity: O(n) where n = visible droplets               ││
        │  │  Typical n: 10-50 (vs theoretical 1000+)                   ││
        │  └─────────────────────────────────────────────────────────────┘│
        └─────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
        ┌─────────────────────────────────────────────────────────────────┐
        │                  🔄 CONCURRENCY ARCHITECTURE                    │
        │                                                                 │
        │  ┌─────────────────────┐  ┌─────────────────────┐              │
        │  │    Main Thread      │  │   Background Tasks  │              │
        │  │                     │  │                     │              │
        │  │ • Game Loop         │  │ • Asset Loading     │              │
        │  │ • Rendering         │  │ • Sound Processing  │              │
        │  │ • Input Handling    │  │ • File I/O          │              │
        │  │ • State Updates     │  │ • Analytics         │              │
        │  │                     │  │                     │              │
        │  │ 🔒 Atomic Ops       │  │ 🔄 Async Ops       │              │
        │  │ (Thread Safe)       │  │ (Non-blocking)      │              │
        │  └─────────────────────┘  └─────────────────────┘              │
        │              │                        │                        │
        │              └────────────┬───────────┘                        │
        │                           │                                    │
        │              ┌─────────────────────┐                          │
        │              │   Clojure STM       │                          │
        │              │ (Software Trans     │                          │
        │              │  Memory)            │                          │
        │              └─────────────────────┘                          │
        └─────────────────────────────────────────────────────────────────┘
```

## 📁 File Structure & Component Mapping

```
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                      📂 PROJECT ARCHITECTURE                          ║
    ╚═══════════════════════════════════════════════════════════════════════╝

    libgdx-drop-game/
    ├── 📄 project.clj ──────────────────┐
    │   └── Dependencies & Build Config   │
    │                                     │
    ├── 📂 src/drop_game/ ───────────────┼─── 💻 CORE APPLICATION
    │   │                                │
    │   ├── 🎯 main.clj ─────────────────┼─── Entry Point & LibGDX Lifecycle
    │   │   ├── ApplicationListener       │    ├── create() - Asset Loading
    │   │   ├── Game Loop Management      │    ├── render() - Main Loop
    │   │   ├── Input/Logic/Draw Pipeline │    ├── resize() - Viewport Updates
    │   │   └── Resource Cleanup          │    └── dispose() - Memory Cleanup
    │   │                                │
    │   ├── 🧠 core.clj ─────────────────┼─── Enhanced Implementation
    │   │   ├── Advanced UI System        │    ├── Multi-font Rendering
    │   │   ├── Creative Scoring          │    ├── Combo System
    │   │   ├── Performance Stats         │    └── State Management
    │   │   └── Error Handling           │
    │   │                                │
    │   ├── 🎮 game.clj ─────────────────┼─── Screen-Based Architecture
    │   │   ├── Screen Interface          │    ├── show() - Initialization
    │   │   ├── Alternative Implementation│    ├── render() - Game Loop
    │   │   └── Modular Design           │    └── hide() - Cleanup
    │   │                                │
    │   └── 🔬 logic.clj ────────────────┼─── Pure Functional Logic
    │       ├── Testable Functions        │    ├── No Side Effects
    │       ├── Game Rules Engine         │    ├── Mathematical Operations
    │       └── Algorithm Implementations │    └── State Transitions
    │                                     │
    ├── 📂 resources/ ──────────────────┼─── 🎨 GAME ASSETS
    │   ├── 🖼️ background.png            │    ├── Visual Assets
    │   ├── 🪣 bucket.png                │    ├── Texture Files
    │   ├── 💧 drop.png                  │    ├── Audio Files
    │   ├── 🔊 drop.mp3                  │    └── Resource Management
    │   └── 🎵 music.mp3                 │
    │                                     │
    └── 📂 target/ ─────────────────────┼─── 🔨 BUILD OUTPUT
        ├── 📦 Compiled Classes           │    ├── JVM Bytecode
        ├── 📚 Dependencies Cache         │    ├── Library JARs
        └── 🚀 Executable JARs           │    └── Distribution Files
```

## 🎯 Architectural Patterns Implementation

```
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                    🏗️ DESIGN PATTERNS IN USE                         ║
    ╚═══════════════════════════════════════════════════════════════════════╝

    ┌─────────────────────────────────────────────────────────────────────┐
    │                 🔄 FUNCTIONAL REACTIVE PROGRAMMING                   │
    │                                                                     │
    │  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐ │
    │  │  Immutable      │    │   Pure          │    │   Reactive      │ │
    │  │   State         │ ──▶│  Functions      │ ──▶│   Updates       │ │
    │  │                 │    │                 │    │                 │ │
    │  │ (defn update-   │    │ (defn calculate-│    │ (swap! score    │ │
    │  │  state [s e]    │    │  score [drops]  │    │  update-fn)     │ │
    │  │  (assoc s       │    │  (* drops 10))  │    │                 │ │
    │  │   :score val))  │    │                 │    │                 │ │
    │  └─────────────────┘    └─────────────────┘    └─────────────────┘ │
    └─────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
    ┌─────────────────────────────────────────────────────────────────────┐
    │                    👁️ OBSERVER PATTERN                              │
    │                                                                     │
    │    State Changes ──▶ Automatic Updates ──▶ Visual Feedback         │
    │                                                                     │
    │  ┌─────────────────────────────────────────────────────────────────┐│
    │  │                                                                 ││
    │  │  @score changes ──▶ UI Score Display Updates                    ││
    │  │       │                      │                                  ││
    │  │       ▼                      ▼                                  ││
    │  │  @combo changes ──▶ Combo Animation Triggers                    ││
    │  │       │                      │                                  ││
    │  │       ▼                      ▼                                  ││
    │  │  @lives changes ──▶ Heart Display Updates                       ││
    │  │                                                                 ││
    │  └─────────────────────────────────────────────────────────────────┘│
    └─────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
    ┌─────────────────────────────────────────────────────────────────────┐
    │                      🎯 STRATEGY PATTERN                            │
    │                                                                     │
    │  Input Strategy Selection:                                          │
    │  ┌─────────────────────────────────────────────────────────────────┐│
    │  │                                                                 ││
    │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐            ││
    │  │  │  Keyboard   │  │   Mouse     │  │   Touch     │            ││
    │  │  │  Strategy   │  │  Strategy   │  │  Strategy   │            ││
    │  │  │             │  │             │  │             │            ││
    │  │  │ • WASD      │  │ • Click     │  │ • Tap       │            ││
    │  │  │ • Arrows    │  │ • Drag      │  │ • Swipe     │            ││
    │  │  │ • Digital   │  │ • Analog    │  │ • Gestures  │            ││
    │  │  └─────────────┘  └─────────────┘  └─────────────┘            ││
    │  │         │                │                │                    ││
    │  │         └────────────────┼────────────────┘                    ││
    │  │                          ▼                                     ││
    │  │              ┌─────────────────────┐                          ││
    │  │              │   Input Handler     │                          ││
    │  │              │    (Unified API)    │                          ││
    │  │              └─────────────────────┘                          ││
    │  └─────────────────────────────────────────────────────────────────┘│
    └─────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
    ┌─────────────────────────────────────────────────────────────────────┐
    │                 🏭 ENTITY-COMPONENT-SYSTEM                          │
    │                                                                     │
    │  Modern Game Architecture Pattern:                                  │
    │  ┌─────────────────────────────────────────────────────────────────┐│
    │  │                                                                 ││
    │  │    🎯 ENTITIES         📦 COMPONENTS        ⚙️ SYSTEMS          ││
    │  │                                                                 ││
    │  │  ┌─────────────┐    ┌─────────────────┐   ┌─────────────────┐  ││
    │  │  │   Bucket    │────│ • Transform     │───│ • Movement      │  ││
    │  │  │   (ID: 1)   │    │ • Input Handler │   │ • Collision     │  ││
    │  │  └─────────────┘    │ • Sprite        │   │ • Rendering     │  ││
    │  │                     │ • Collider      │   └─────────────────┘  ││
    │  │  ┌─────────────┐    └─────────────────┘                        ││
    │  │  │  Droplet    │                                               ││
    │  │  │  (ID: 2-N)  │    ┌─────────────────┐   ┌─────────────────┐  ││
    │  │  └─────────────┘────│ • Transform     │───│ • Physics       │  ││
    │  │                     │ • Velocity      │   │ • Collision     │  ││
    │  │                     │ • Sprite        │   │ • Lifecycle     │  ││
    │  │                     │ • Collider      │   └─────────────────┘  ││
    │  │                     └─────────────────┘                        ││
    │  └─────────────────────────────────────────────────────────────────┘│
    └─────────────────────────────────────────────────────────────────────┘
```

## 🔧 Integration Architecture

```
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                      🔗 TECHNOLOGY INTEGRATION                        ║
    ╚═══════════════════════════════════════════════════════════════════════╝

    ┌─────────────────────────────────────────────────────────────────────┐
    │                       ☕ JVM ECOSYSTEM                               │
    │                                                                     │
    │  ┌─────────────────┐         ┌─────────────────┐                    │
    │  │    Clojure      │◀────────│      Java       │                    │
    │  │    Runtime      │  Interop │     Libraries   │                    │
    │  │                 │         │                 │                    │
    │  │ • Functional    │         │ • LibGDX Engine │                    │
    │  │   Programming   │         │ • OpenGL Bind   │                    │
    │  │ • Immutable     │         │ • Audio System  │                    │
    │  │   Data Structs  │         │ • File I/O      │                    │
    │  │ • STM           │         │ • Math Utils    │                    │
    │  └─────────────────┘         └─────────────────┘                    │
    │           │                           │                             │
    │           └───────────┬───────────────┘                             │
    │                       ▼                                             │
    │              ┌─────────────────┐                                    │
    │              │  JVM Bytecode   │                                    │
    │              │   (Compiled)    │                                    │
    │              └─────────────────┘                                    │
    └─────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
    ┌─────────────────────────────────────────────────────────────────────┐
    │                      🎮 LIBGDX INTEGRATION                          │
    │                                                                     │
    │  ┌─────────────────────────────────────────────────────────────────┐│
    │  │                                                                 ││
    │  │     📱 Platform Abstraction Layer                               ││
    │  │                                                                 ││
    │  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐  ││
    │  │  │Desktop  │ │ Android │ │  iOS    │ │ WebGL   │ │  Switch │  ││
    │  │  │(LWJGL3) │ │         │ │(RoboVM) │ │(GWT)    │ │         │  ││
    │  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘  ││
    │  │       │           │           │           │           │        ││
    │  │       └───────────┼───────────┼───────────┼───────────┘        ││
    │  │                   └───────────┼───────────┘                    ││
    │  │                               ▼                                ││
    │  │              ┌─────────────────────────┐                       ││
    │  │              │    LibGDX Core API      │                       ││
    │  │              │                         │                       ││
    │  │              │ • Graphics (OpenGL)     │                       ││
    │  │              │ • Audio (OpenAL)        │                       ││
    │  │              │ • Input (Multi-device)  │                       ││
    │  │              │ • Files (VFS)           │                       ││
    │  │              │ • Network (HTTP/TCP)    │                       ││
    │  │              └─────────────────────────┘                       ││
    │  └─────────────────────────────────────────────────────────────────┘│
    └─────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
    ┌─────────────────────────────────────────────────────────────────────┐
    │                    🔄 CLOJURE INTEGRATION                           │
    │                                                                     │
    │  ┌─────────────────────────────────────────────────────────────────┐│
    │  │                                                                 ││
    │  │         Functional ◀────▶ Object-Oriented                       ││
    │  │        Programming        Programming                            ││
    │  │                                                                 ││
    │  │  ┌─────────────────┐    ┌─────────────────┐                    ││
    │  │  │   Clojure       │    │     LibGDX      │                    ││
    │  │  │   Functions     │    │     Objects     │                    ││
    │  │  │                 │    │                 │                    ││
    │  │  │ (defn update    │◀──▶│ .setPosition    │                    ││
    │  │  │  [entity data]  │    │ .translate      │                    ││
    │  │  │  ...)           │    │ .overlaps       │                    ││
    │  │  │                 │    │                 │                    ││
    │  │  │ • Pure Logic    │    │ • Stateful Ops  │                    ││
    │  │  │ • Immutable     │    │ • Mutable State │                    ││
    │  │  │ • Testable      │    │ • Performance   │                    ││
    │  │  └─────────────────┘    └─────────────────┘                    ││
    │  │           │                       │                            ││
    │  │           └───────────┬───────────┘                            ││
    │  │                       ▼                                        ││
    │  │              ┌─────────────────┐                               ││
    │  │              │  Hybrid Approach │                               ││
    │  │              │ (Best of Both)  │                               ││
    │  │              └─────────────────┘                               ││
    │  └─────────────────────────────────────────────────────────────────┘│
    └─────────────────────────────────────────────────────────────────────┘
```

## 🚀 Deployment & Build Architecture

```
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                        🏭 BUILD PIPELINE                             ║
    ╚═══════════════════════════════════════════════════════════════════════╝

    ┌─────────────────────────────────────────────────────────────────────┐
    │                    📝 DEVELOPMENT WORKFLOW                          │
    │                                                                     │
    │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐            │
    │  │   Source    │    │   REPL      │    │   Testing   │            │
    │  │   Code      │ ──▶│ Development │ ──▶│ Validation  │            │
    │  │             │    │             │    │             │            │
    │  │ • .clj      │    │ • Live      │    │ • Unit      │            │
    │  │   Files     │    │   Coding    │    │   Tests     │            │
    │  │ • Hot       │    │ • Instant   │    │ • Logic     │            │
    │  │   Reload    │    │   Feedback  │    │   Verify    │            │
    │  └─────────────┘    └─────────────┘    └─────────────┘            │
    └─────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
    ┌─────────────────────────────────────────────────────────────────────┐
    │                     🔨 LEININGEN BUILD SYSTEM                       │
    │                                                                     │
    │  ┌─────────────────────────────────────────────────────────────────┐│
    │  │                     project.clj                                 ││
    │  │                                                                 ││
    │  │  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐   ││
    │  │  │ Dependencies    │ │  Build Config   │ │    Profiles     │   ││
    │  │  │                 │ │                 │ │                 │   ││
    │  │  │ • Clojure 1.12  │ │ • Source Paths  │ │ • Development   │   ││
    │  │  │ • LibGDX 1.12   │ │ • Resource Dir  │ │ • Production    │   ││
    │  │  │ • LWJGL3        │ │ • Main Class    │ │ • Uberjar       │   ││
    │  │  │ • Natives       │ │ • JVM Options   │ │ • AOT Compile   │   ││
    │  │  └─────────────────┘ └─────────────────┘ └─────────────────┘   ││
    │  └─────────────────────────────────────────────────────────────────┘│
    └─────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
    ┌─────────────────────────────────────────────────────────────────────┐
    │                       📦 BUILD ARTIFACTS                            │
    │                                                                     │
    │  Build Outputs:                                                     │
    │  ┌─────────────────────────────────────────────────────────────────┐│
    │  │                                                                 ││
    │  │  Development Mode:                                              ││
    │  │  ├─ target/classes/        (Compiled Clojure)                   ││
    │  │  ├─ target/stale/          (Build metadata)                     ││
    │  │  └─ .nrepl-port            (REPL connection)                    ││
    │  │                                                                 ││
    │  │  Production Mode:                                               ││
    │  │  ├─ target/uberjar/        (Standalone JAR)                     ││
    │  │  ├─ All dependencies       (Bundled libraries)                  ││
    │  │  ├─ Native libraries       (Platform binaries)                  ││
    │  │  └─ Resources embedded     (Game assets)                        ││
    │  │                                                                 ││
    │  │  Distribution:                                                  ││
    │  │  ├─ executable.jar         (Double-click to run)                ││
    │  │  ├─ installer.exe          (Windows installer)                  ││
    │  │  ├─ app.dmg                (macOS package)                      ││
    │  │  └─ game.deb                (Linux package)                      ││
    │  └─────────────────────────────────────────────────────────────────┘│
    └─────────────────────────────────────────────────────────────────────┘
```

## 🔍 Quality Assurance Architecture

```
    ╔═══════════════════════════════════════════════════════════════════════╗
    ║                     ✅ TESTING & QA STRATEGY                         ║
    ╚═══════════════════════════════════════════════════════════════════════╝

    ┌─────────────────────────────────────────────────────────────────────┐
    │                      🧪 TESTING PYRAMID                             │
    │                                                                     │
    │                         ┌─────────────┐                            │
    │                         │     E2E     │ ← Full Game Testing        │
    │                         │   Tests     │   (Manual QA)              │
    │                         └─────────────┘                            │
    │                       ┌─────────────────┐                          │
    │                       │  Integration    │ ← Component Testing      │
    │                       │     Tests       │   (LibGDX + Logic)       │
    │                       └─────────────────┘                          │
    │                   ┌─────────────────────────┐                      │
    │                   │      Unit Tests         │ ← Pure Function Tests │
    │                   │   (logic.clj focused)   │   (Fast & Reliable)   │
    │                   └─────────────────────────┘                      │
    └─────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
    ┌─────────────────────────────────────────────────────────────────────┐
    │                   🔬 PURE FUNCTION TESTING                          │
    │                                                                     │
    │  logic.clj → Testable Functions (No Side Effects)                   │
    │  ┌─────────────────────────────────────────────────────────────────┐│
    │  │                                                                 ││
    │  │  (deftest collision-detection-test                              ││
    │  │    (testing "AABB collision algorithm"                          ││
    │  │      (is (true? (rectangles-overlap?                            ││
    │  │                   {:x 0 :y 0 :width 10 :height 10}             ││
    │  │                   {:x 5 :y 5 :width 10 :height 10})))          ││
    │  │      (is (false? (rectangles-overlap?                           ││
    │  │                    {:x 0 :y 0 :width 5 :height 5}              ││
    │  │                    {:x 10 :y 10 :width 5 :height 5})))))       ││
    │  │                                                                 ││
    │  │  (deftest scoring-system-test                                   ││
    │  │    (testing "Combo multiplier calculation"                      ││
    │  │      (is (= 20 (calculate-score 2 10 2)))  ; 2 drops, 10 pts, 2x││
    │  │      (is (= 1 (reset-multiplier 0)))))     ; No combo           ││
    │  └─────────────────────────────────────────────────────────────────┘│
    └─────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
    ┌─────────────────────────────────────────────────────────────────────┐
    │                    ⚡ PERFORMANCE MONITORING                         │
    │                                                                     │
    │  ┌─────────────────────────────────────────────────────────────────┐│
    │  │                                                                 ││
    │  │    📊 Metrics Collection:                                       ││
    │  │                                                                 ││
    │  │    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐            ││
    │  │    │ Frame Rate  │ │   Memory    │ │ Collision   │            ││
    │  │    │ Monitoring  │ │    Usage    │ │Performance  │            ││
    │  │    │             │ │             │ │             │            ││
    │  │    │ Target:     │ │ Heap: <512M │ │ O(n) where  │            ││
    │  │    │ 60+ FPS     │ │ GC: <100ms  │ │ n = droplets│            ││
    │  │    │ 1% drops    │ │ Stable mem  │ │ Time: <1ms  │            ││
    │  │    └─────────────┘ └─────────────┘ └─────────────┘            ││
    │  │                                                                 ││
    │  │    🎯 Quality Gates:                                            ││
    │  │    ├─ Startup time < 3 seconds                                  ││
    │  │    ├─ Crash rate < 0.1%                                        ││
    │  │    ├─ Battery drain acceptable                                  ││
    │  │    └─ Platform compatibility 99%+                              ││
    │  └─────────────────────────────────────────────────────────────────┘│
    └─────────────────────────────────────────────────────────────────────┘
```

This enhanced architecture documentation with comprehensive ASCII art provides a complete visual understanding of the Enhanced Drop Game's technical architecture, from high-level system design down to implementation details and quality assurance strategies.