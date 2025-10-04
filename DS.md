# Data Structures Documentation

## Overview
This document details all data structures used in the Enhanced Drop Game, their purposes, relationships, and performance characteristics. The game leverages both Clojure's immutable data structures and LibGDX's mutable objects for optimal performance.

## Core Game State Data Structures

### **Game State Atoms**
```clojure
;; Primary game state management using Clojure atoms
(def score (atom 0))                    ; Current player score
(def high-score (atom 0))               ; Best score achieved
(def drops-caught (atom 0))             ; Total successful catches
(def drops-missed (atom 0))             ; Total missed drops
(def game-time (atom 0.0))              ; Elapsed game time
(def score-multiplier (atom 1))         ; Current score multiplier
(def combo-count (atom 0))              ; Current combo streak
(def show-combo (atom false))           ; Combo display flag
(def combo-timer (atom 0.0))           ; Combo display duration
```

**Characteristics:**
- **Thread-Safe**: Atomic updates prevent race conditions
- **Immutable**: State changes create new values, not mutations
- **Observable**: Changes can trigger reactive updates
- **Performance**: O(1) read access, lock-free updates

### **Asset Management Structures**
```clojure
;; LibGDX asset references stored in atoms
(def ^Texture background-texture (atom nil))
(def ^Texture bucket-texture (atom nil))
(def ^Texture drop-texture (atom nil))
(def ^Sound drop-sound (atom nil))
(def ^Music music (atom nil))
```

**Memory Model:**
- **Reference Storage**: Atoms hold references to LibGDX objects
- **Lazy Loading**: Assets loaded on-demand during initialization
- **Lifecycle Management**: Proper disposal in cleanup phase

## Game Object Data Structures

### **Sprite Collections**
```clojure
;; LibGDX Array for efficient sprite management
(def ^Array drop-sprites (atom nil))    ; Dynamic array of droplet sprites
(def ^Sprite bucket-sprite (atom nil))  ; Single bucket sprite reference
```

**LibGDX Array Characteristics:**
- **Type**: `com.badlogic.gdx.utils.Array`
- **Performance**: O(1) access, O(1) append, O(n) removal
- **Memory**: Contiguous memory layout for cache efficiency
- **Growth**: Dynamic resizing with exponential growth

### **Collision Detection Structures**
```clojure
;; Rectangle objects for collision detection
(def ^Rectangle bucket-rectangle (atom nil))
(def ^Rectangle drop-rectangle (atom nil))
```

**Rectangle Properties:**
```clojure
{:x float        ; X coordinate (bottom-left)
 :y float        ; Y coordinate (bottom-left)
 :width float    ; Width in world units
 :height float}  ; Height in world units
```

**Collision Algorithm**: Axis-Aligned Bounding Box (AABB)
- **Time Complexity**: O(1) per collision check
- **Space Complexity**: O(1) per entity
- **Precision**: Pixel-perfect rectangular collision

## Rendering Data Structures

### **Viewport Management**
```clojure
(def ^FitViewport viewport (atom nil))      ; Game world viewport
(def ^ScreenViewport ui-viewport (atom nil)) ; UI overlay viewport
```

**FitViewport Structure:**
```clojure
{:world-width float     ; Virtual world width
 :world-height float    ; Virtual world height
 :camera Camera         ; Orthographic camera
 :screen-x int          ; Screen position X
 :screen-y int          ; Screen position Y
 :screen-width int      ; Screen width in pixels
 :screen-height int}    ; Screen height in pixels
```

### **Font Rendering System**
```clojure
(def ^BitmapFont title-font (atom nil))   ; Large title text
(def ^BitmapFont score-font (atom nil))   ; Score display text
(def ^BitmapFont ui-font (atom nil))      ; General UI text
(def ^GlyphLayout glyph-layout (atom nil)) ; Text measurement
```

**Font Data Structure:**
```clojure
BitmapFont {
  :texture-regions Vector    ; Glyph texture atlas
  :glyph-data Map           ; Character metrics
  :line-height float        ; Vertical spacing
  :space-width float        ; Space character width
  :x-height float           ; Lowercase letter height
  :cap-height float}        ; Uppercase letter height
```

## Input Data Structures

### **Input State Management**
```clojure
(def ^Vector2 touch-pos (atom nil))     ; Mouse/touch position
```

**Vector2 Structure:**
```clojure
{:x float    ; X coordinate in world space
 :y float}   ; Y coordinate in world space
```

**Input Processing Pipeline:**
```
Screen Coordinates → Viewport Unprojection → World Coordinates → Game Logic
```

## Performance-Optimized Collections

### **Droplet Management System**
```clojure
;; Efficient droplet lifecycle management
Array<Sprite> drop-sprites {
  :size int                 ; Current number of droplets
  :items Sprite[]          ; Internal array storage
  :ordered boolean         ; Maintains insertion order
}
```

**Operations & Complexity:**
- **Add Droplet**: O(1) amortized
- **Remove Droplet**: O(n) due to array compaction
- **Iterate Droplets**: O(n) with cache-friendly access
- **Clear All**: O(1) reset operation

### **Memory Pool Pattern** (Conceptual)
```clojure
;; Future optimization: Object pooling for droplets
ObjectPool<Rectangle> {
  :available Queue<Rectangle>    ; Ready-to-use objects
  :in-use Set<Rectangle>        ; Currently active objects
  :max-size int                 ; Pool capacity limit
}
```

## Game Logic Data Structures

### **Timer Management**
```clojure
;; Delta-time based timing system
{:drop-timer float          ; Time until next droplet spawn
 :game-time float           ; Total elapsed game time  
 :last-score-time float     ; Timestamp of last score
 :combo-timer float}        ; Combo display countdown
```

### **Scoring System**
```clojure
;; Hierarchical scoring data
{:base-score int            ; Points per catch (10)
 :multiplier int            ; Current score multiplier
 :combo-count int           ; Consecutive catches
 :accuracy float            ; Percentage accuracy
 :performance-stats {
   :total-drops int         ; Drops spawned
   :caught-drops int        ; Successfully caught
   :missed-drops int        ; Fell off screen
   :perfect-catches int}}   ; Caught immediately
```

## Configuration Data Structures

### **Game Constants**
```clojure
(def game-config {
  :screen-width 800
  :screen-height 500
  :world-width 8.0
  :world-height 5.0
  :bucket-speed 4.0
  :drop-speed 2.0
  :spawn-interval 1.0
  :combo-threshold 3
  :multiplier-increment 1})
```

### **Color Palette**
```clojure
(def ui-colors {
  :title Color/YELLOW        ; Game title
  :score Color/CYAN          ; Current score
  :high-score Color/GOLD     ; Best score
  :combo Color/RED           ; Combo display
  :accuracy-good Color/GREEN ; High accuracy
  :accuracy-ok Color/YELLOW  ; Medium accuracy
  :accuracy-poor Color/RED}) ; Low accuracy
```

## Memory Layout & Optimization

### **Cache-Friendly Design**
```
Game Objects (Hot Path):
┌─────────────────────────────────────┐
│ Bucket Sprite │ Drop Array │ Rects │  ← Frequently accessed
├─────────────────────────────────────┤
│ Game State Atoms │ Timers │ Score │  ← Moderate access
├─────────────────────────────────────┤
│ Assets │ Fonts │ UI Elements       │  ← Less frequent access
└─────────────────────────────────────┘
```

### **Garbage Collection Optimization**
- **Immutable Structures**: Minimize object creation in game loop
- **Object Reuse**: Reuse Rectangle objects for collision detection
- **Batch Operations**: Group sprite updates to reduce allocation
- **Atom Updates**: Use swap! for efficient state transitions

## Data Flow Architecture

### **State Update Pipeline**
```
Input Events → State Validation → Atomic Updates → Reactive Rendering
     ↓                ↓                ↓                ↓
[Keyboard/Mouse] → [Game Rules] → [Atom swap!] → [UI Refresh]
```

### **Collision Detection Pipeline**
```
Entity Positions → Bounding Box Update → Intersection Test → Game Event
       ↓                   ↓                   ↓              ↓
   [Sprite.getX/Y] → [Rectangle.set] → [Rectangle.overlaps] → [Score++]
```

## Thread Safety & Concurrency

### **Concurrent Access Patterns**
```clojure
;; Safe atomic updates
(swap! score + (* 10 @score-multiplier))    ; Atomic score increment
(reset! combo-timer 2.0)                    ; Atomic timer reset
(compare-and-set! high-score old new)       ; Conditional update
```

### **Data Race Prevention**
- **Atom Semantics**: All state mutations use atomic operations
- **Read Consistency**: Snapshot reads prevent mid-update observations
- **Write Ordering**: Sequential consistency guarantees

## Serialization & Persistence

### **High Score Persistence** (Future)
```clojure
{:player-name String
 :score int
 :timestamp long
 :accuracy float
 :play-time float
 :combo-record int}
```

### **Game State Snapshots**
```clojure
;; Serializable game state for save/load
{:version String
 :current-score int
 :game-time float
 :difficulty-level int
 :active-droplets Vector
 :player-stats Map}
```

This data structure design provides excellent performance characteristics while maintaining code clarity and extensibility for future enhancements.