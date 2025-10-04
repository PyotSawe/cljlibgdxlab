(ns drop-game.logic
  "Pure functional game logic without libGDX dependencies for testing")

;; Game constants
(def ^:const SCREEN-WIDTH 800)
(def ^:const SCREEN-HEIGHT 480)
(def ^:const BUCKET-WIDTH 64)
(def ^:const BUCKET-HEIGHT 64)
(def ^:const RAINDROP-WIDTH 64)
(def ^:const RAINDROP-HEIGHT 64)
(def ^:const BUCKET-SPEED 200)
(def ^:const RAINDROP-SPEED 200)

(defn create-game-state []
  "Create initial game state"
  {:bucket-x (/ (- SCREEN-WIDTH BUCKET-WIDTH) 2)
   :bucket-y 20
   :raindrops []
   :last-drop-time 0
   :score 0})

(defn create-raindrop [x]
  "Create a raindrop at given x position"
  {:x x
   :y SCREEN-HEIGHT
   :width RAINDROP-WIDTH
   :height RAINDROP-HEIGHT})

(defn rectangles-overlap? [rect1 rect2]
  "Check if two rectangles overlap"
  (and (< (:x rect1) (+ (:x rect2) (:width rect2)))
       (< (:x rect2) (+ (:x rect1) (:width rect1)))
       (< (:y rect1) (+ (:y rect2) (:height rect2)))
       (< (:y rect2) (+ (:y rect1) (:height rect1)))))

(defn move-bucket [game-state direction delta-time]
  "Move bucket left (-1) or right (1)"
  (let [bucket-x (:bucket-x game-state)
        movement (* direction BUCKET-SPEED delta-time)
        new-x (+ bucket-x movement)
        clamped-x (max 0 (min (- SCREEN-WIDTH BUCKET-WIDTH) new-x))]
    (assoc game-state :bucket-x clamped-x)))

(defn update-raindrops [game-state delta-time]
  "Update raindrop positions and check collisions"
  (let [bucket {:x (:bucket-x game-state)
                :y (:bucket-y game-state)
                :width BUCKET-WIDTH
                :height BUCKET-HEIGHT}
        updated-drops (map (fn [drop]
                            (assoc drop :y (- (:y drop) (* RAINDROP-SPEED delta-time))))
                          (:raindrops game-state))
        {caught true, remaining false} (group-by #(rectangles-overlap? % bucket) updated-drops)
        on-screen (filter #(> (+ (:y %) RAINDROP-HEIGHT) 0) remaining)]
    (-> game-state
        (assoc :raindrops on-screen)
        (update :score + (count caught)))))

(defn spawn-raindrop [game-state current-time]
  "Add a new raindrop if enough time has passed"
  (if (> (- current-time (:last-drop-time game-state)) 1000000000) ; 1 second in nanoseconds
    (let [x (rand-int (- SCREEN-WIDTH RAINDROP-WIDTH))
          new-drop (create-raindrop x)]
      (-> game-state
          (update :raindrops conj new-drop)
          (assoc :last-drop-time current-time)))
    game-state))

(defn update-game [game-state delta-time current-time input]
  "Main game update function"
  (-> game-state
      (move-bucket (:move-direction input 0) delta-time)
      (update-raindrops delta-time)
      (spawn-raindrop current-time)))

;; Test functions
(defn run-game-simulation [steps]
  "Run a simple game simulation for testing"
  (loop [state (create-game-state)
         step 0]
    (if (>= step steps)
      state
      (let [delta-time 0.016 ; ~60 FPS
            current-time (* step 16666667) ; Mock time in nanoseconds
            input {:move-direction (rand-nth [-1 0 1])}] ; Random movement
        (recur (update-game state delta-time current-time input)
               (inc step))))))

(comment
  ;; Test the game logic
  (create-game-state)
  (create-raindrop 100)
  (rectangles-overlap? {:x 0 :y 0 :width 64 :height 64}
                      {:x 32 :y 32 :width 64 :height 64})
  (run-game-simulation 100)
  )