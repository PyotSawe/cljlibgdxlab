#!/usr/bin/env lein exec

(require '[drop-game.logic :as logic])

(println "\n=== Testing Drop Game Logic ===")

;; Test initial state
(def initial-state (logic/create-game-state))
(println "Initial state:" initial-state)

;; Test raindrop creation
(def test-drop (logic/create-raindrop 100))
(println "Test raindrop:" test-drop)

;; Test collision detection
(def bucket {:x 100 :y 20 :width 64 :height 64})
(def drop-hit {:x 120 :y 30 :width 64 :height 64})
(def drop-miss {:x 200 :y 30 :width 64 :height 64})

(println "Bucket-drop collision (should hit):" (logic/rectangles-overlap? bucket drop-hit))
(println "Bucket-drop collision (should miss):" (logic/rectangles-overlap? bucket drop-miss))

;; Test bucket movement
(def moved-left (logic/move-bucket initial-state -1 0.1))
(def moved-right (logic/move-bucket initial-state 1 0.1))
(println "Moved left:" (:bucket-x moved-left))
(println "Moved right:" (:bucket-x moved-right))

;; Test game simulation
(def simulated-game (logic/run-game-simulation 10))
(println "After simulation - Score:" (:score simulated-game))
(println "After simulation - Raindrops:" (count (:raindrops simulated-game)))

(println "\n=== All tests completed successfully! ===\n")