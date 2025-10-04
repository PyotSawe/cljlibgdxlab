# Algorithms Documentation

## Overview
This document details all algorithms implemented in the Enhanced Drop Game, covering physics simulation, collision detection, game mechanics, and modern game development techniques commonly found in contemporary games.

## Core Game Algorithms

### 1. **Physics & Movement Algorithms**

#### **Delta-Time Based Movement**
```clojure
;; Framerate-independent movement algorithm
(defn update-position [entity delta-time]
  (let [velocity (:velocity entity)
        position (:position entity)]
    (+ position (* velocity delta-time))))
```

**Algorithm Details:**
- **Type**: Numerical Integration (Euler's Method)
- **Time Complexity**: O(1)
- **Purpose**: Ensures consistent movement regardless of frame rate
- **Implementation**: `position += velocity * deltaTime`

#### **Gravity Simulation**
```clojure
;; Simplified gravity for droplet falling
(defn apply-gravity [droplet delta-time]
  (let [gravity-force -2.0  ; World units per second squared
        current-velocity (:velocity-y droplet)]
    (assoc droplet :velocity-y (+ current-velocity (* gravity-force delta-time)))))
```

**Physics Model:**
- **Algorithm**: Constant Acceleration Physics
- **Formula**: `v = v₀ + at`
- **Realism**: Simplified for gameplay (no air resistance)

#### **Boundary Constraint Algorithm**
```clojure
;; Keep bucket within screen bounds
(defn clamp-position [position min-bound max-bound]
  (max min-bound (min max-bound position)))
```

**Mathematical Model:**
```
clamp(x, min, max) = {
  min   if x < min
  max   if x > max  
  x     otherwise
}
```

### 2. **Collision Detection Algorithms**

#### **Axis-Aligned Bounding Box (AABB)**
```clojure
(defn aabb-collision? [rect1 rect2]
  (and (< (:x rect1) (+ (:x rect2) (:width rect2)))
       (< (:x rect2) (+ (:x rect1) (:width rect1)))
       (< (:y rect1) (+ (:y rect2) (:height rect2)))
       (< (:y rect2) (+ (:y rect1) (:height rect1)))))
```

**Algorithm Analysis:**
- **Type**: Separating Axis Theorem (2D simplified)
- **Time Complexity**: O(1) per collision check
- **Space Complexity**: O(1)
- **Accuracy**: Perfect for rectangular objects
- **Performance**: Highly optimized for 2D games

#### **Collision Response Algorithm**
```clojure
(defn handle-collision [bucket droplet]
  {:remove-droplet true
   :score-increase (* 10 @score-multiplier)
   :play-sound true
   :trigger-effects [:particle-explosion :screen-shake]})
```

### 3. **Game State Management Algorithms**

#### **Finite State Machine (Game States)**
```clojure
;; Game state transitions
(def game-states {:menu 0 :playing 1 :paused 2 :game-over 3})

(defn transition-state [current-state event]
  (case [current-state event]
    [:menu :start] :playing
    [:playing :pause] :paused
    [:playing :game-over] :game-over
    [:paused :resume] :playing
    current-state))
```

**FSM Properties:**
- **States**: Discrete game modes
- **Transitions**: Event-driven state changes
- **Validation**: Only valid transitions allowed

#### **Atomic State Updates**
```clojure
;; Compare-and-swap algorithm for thread-safe updates
(defn safe-score-update [score-atom points]
  (swap! score-atom 
    (fn [current-score]
      (let [new-score (+ current-score points)]
        (when (> new-score @high-score)
          (reset! high-score new-score))
        new-score))))
```

## Scoring & Progression Algorithms

### **Combo System Algorithm**
```clojure
(defn update-combo [combo-count time-since-last-catch]
  (let [combo-timeout 2.0] ; seconds
    (if (< time-since-last-catch combo-timeout)
      (inc combo-count)      ; Continue combo
      0)))                   ; Reset combo
```

**Combo Mechanics:**
- **Temporal Window**: Time-based combo validation
- **Exponential Scoring**: Score = base × multiplier^combo
- **Visual Feedback**: Dynamic UI updates for combo streaks

### **Dynamic Difficulty Scaling**
```clojure
(defn calculate-spawn-rate [game-time score]
  (let [base-rate 1.0          ; drops per second
        time-factor 0.1        ; increases over time
        score-factor 0.001]    ; increases with score
    (+ base-rate 
       (* time-factor (Math/sqrt game-time))
       (* score-factor score))))
```

**Adaptive Difficulty:**
- **Time Progression**: Difficulty increases with play time
- **Performance-Based**: Adjusts to player skill level
- **Smooth Scaling**: Prevents sudden difficulty spikes

### **Score Multiplier Algorithm**
```clojure
(defn calculate-multiplier [combo-count accuracy]
  (let [combo-multiplier (Math/floor (/ combo-count 5))
        accuracy-bonus (if (> accuracy 0.9) 2 1)]
    (* (+ 1 combo-multiplier) accuracy-bonus)))
```

## Modern Game Development Algorithms

### 1. **Object Pooling Algorithm**
```clojure
;; Memory-efficient object reuse
(defprotocol ObjectPool
  (acquire [pool] "Get an object from the pool")
  (release [pool obj] "Return an object to the pool"))

(defn create-droplet-pool [size]
  {:available (atom (vec (repeatedly size create-droplet)))
   :active (atom #{})})
```

**Benefits:**
- **Garbage Collection**: Reduces GC pressure
- **Performance**: Eliminates allocation/deallocation overhead
- **Memory**: Predictable memory usage patterns

### 2. **Spatial Partitioning (Future Enhancement)**
```clojure
;; Quadtree for efficient collision detection
(defrecord Quadtree [bounds objects children])

(defn insert-object [qtree object]
  (if (can-subdivide? qtree)
    (subdivide-and-insert qtree object)
    (add-to-leaf qtree object)))
```

**Optimization:**
- **Time Complexity**: O(log n) for collision queries
- **Space Partitioning**: Divide world into spatial regions
- **Broad Phase**: Quick elimination of impossible collisions

### 3. **Entity-Component-System (ECS) Pattern**
```clojure
;; Modern game architecture pattern
(defrecord Entity [id components])
(defrecord Component [type data])

(defn update-physics-system [entities delta-time]
  (map #(update-if-has-component % :physics update-physics delta-time)
       entities))
```

### 4. **Performance Profiling Algorithms**
```clojure
;; Runtime performance monitoring
(defn measure-performance [f]
  (let [start-time (System/nanoTime)
        result (f)
        end-time (System/nanoTime)
        duration (- end-time start-time)]
    {:result result
     :duration-ns duration
     :duration-ms (/ duration 1000000.0)}))
```

## Input Processing Algorithms

### **Input Smoothing & Interpolation**
```clojure
;; Smooth input for responsive controls
(defn smooth-input [current-pos target-pos smoothing-factor delta-time]
  (let [diff (- target-pos current-pos)
        movement (* diff smoothing-factor delta-time)]
    (+ current-pos movement)))
```

### **Input Prediction Algorithm**
```clojure
;; Predict player movement for responsive gameplay
(defn predict-bucket-position [current-pos velocity delta-time]
  (+ current-pos (* velocity delta-time)))
```

## Rendering Optimization Algorithms

### **Frustum Culling**
```clojure
;; Don't render objects outside camera view
(defn in-camera-bounds? [object camera]
  (let [cam-bounds (camera-bounds camera)
        obj-bounds (object-bounds object)]
    (rectangles-intersect? cam-bounds obj-bounds)))
```

### **Batch Rendering Optimization**
```clojure
;; Minimize GPU draw calls
(defn batch-render [sprites]
  (.begin sprite-batch)
  (doseq [sprite sprites]
    (.draw sprite sprite-batch))
  (.end sprite-batch))
```

## AI & Behavior Algorithms

### **Simple AI Patterns (For Future NPCs)**
```clojure
;; State-based AI behavior
(defn ai-behavior [entity player-pos]
  (case (:state entity)
    :idle (idle-behavior entity)
    :chase (chase-behavior entity player-pos)
    :flee (flee-behavior entity player-pos)))
```

### **Pathfinding (A* Algorithm Template)**
```clojure
;; A* pathfinding for complex AI movement
(defn a-star [start goal heuristic-fn neighbor-fn]
  (loop [open-set #{start}
         came-from {}
         g-score {start 0}
         f-score {start (heuristic-fn start goal)}]
    ;; A* implementation details...
    ))
```

## Mathematical Algorithms

### **Linear Interpolation (LERP)**
```clojure
(defn lerp [start end t]
  (+ start (* t (- end start))))
```

### **Easing Functions**
```clojure
;; Smooth animation transitions
(defn ease-in-out-cubic [t]
  (if (< t 0.5)
    (* 4 t t t)
    (- 1 (* (Math/pow (* -2 t) 2) 2))))
```

### **Random Number Generation**
```clojure
;; Deterministic randomness for reproducible gameplay
(defn seeded-random [seed]
  (java.util.Random. seed))

(defn weighted-random [weights]
  (let [total (reduce + weights)
        rand-val (* (Math/random) total)]
    ;; Select based on weighted probability
    ))
```

## Audio Algorithms

### **Audio Mixing Algorithm**
```clojure
;; Simple audio channel management
(defn play-sound-with-priority [sound priority]
  (when (> priority @current-priority)
    (.play sound)
    (reset! current-priority priority)))
```

### **Dynamic Audio Adjustment**
```clojure
;; Adjust audio based on game state
(defn calculate-audio-volume [distance max-distance]
  (max 0.0 (- 1.0 (/ distance max-distance))))
```

## Performance Optimization Algorithms

### **Frame Rate Independent Updates**
```clojure
;; Accumulator pattern for stable physics
(defn fixed-timestep-update [accumulator delta-time fixed-dt update-fn]
  (loop [acc (+ accumulator delta-time)]
    (if (>= acc fixed-dt)
      (do
        (update-fn fixed-dt)
        (recur (- acc fixed-dt)))
      acc)))
```

### **Memory Management**
```clojure
;; Efficient memory usage patterns
(defn manage-object-lifecycle [objects max-objects]
  (if (> (count objects) max-objects)
    (take max-objects objects)  ; Keep only recent objects
    objects))
```

## Algorithm Complexity Summary

| Algorithm | Time Complexity | Space Complexity | Use Case |
|-----------|----------------|------------------|----------|
| AABB Collision | O(1) | O(1) | Real-time collision detection |
| Delta-time Movement | O(1) | O(1) | Smooth animation |
| Combo System | O(1) | O(1) | Score multipliers |
| Object Pooling | O(1) | O(n) | Memory optimization |
| Quadtree Insert | O(log n) | O(n) | Spatial optimization |
| A* Pathfinding | O(b^d) | O(b^d) | AI navigation |
| Lerp/Easing | O(1) | O(1) | Smooth transitions |

These algorithms provide a solid foundation for game development and can be extended or optimized based on specific performance requirements and feature additions.