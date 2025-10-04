(ns drop-game.game
  (:import [com.badlogic.gdx Screen Gdx Input$Keys]
           [com.badlogic.gdx.graphics GL20 OrthographicCamera Texture Color]
           [com.badlogic.gdx.graphics.g2d SpriteBatch]
           [com.badlogic.gdx.math Vector2 Rectangle MathUtils]
           [com.badlogic.gdx.utils Array TimeUtils]
           [com.badlogic.gdx.audio Sound Music]))

;; Game constants
(def ^:const SCREEN-WIDTH 800)
(def ^:const SCREEN-HEIGHT 480)
(def ^:const BUCKET-WIDTH 64)
(def ^:const BUCKET-HEIGHT 64)
(def ^:const RAINDROP-WIDTH 64)
(def ^:const RAINDROP-HEIGHT 64)
(def ^:const BUCKET-SPEED 200)
(def ^:const RAINDROP-SPEED 200)

;; Game as a system has states
(def game-state
  (atom {:bucket-x (/ (- SCREEN-WIDTH BUCKET-WIDTH) 2)
         :bucket-y 20
         :raindrops (Array.)
         :last-drop-time 0
         :score 0
         :drop-sound nil
         :rain-music nil
         :bucket-image nil
         :droplet-image nil}))
;; Create its bucket-image
(defn create-bucket-image []
  "Create a simple blue rectangle texture for the bucket"
  (let [pixmap (com.badlogic.gdx.graphics.Pixmap. BUCKET-WIDTH BUCKET-HEIGHT
                                                   com.badlogic.gdx.graphics.Pixmap$Format/RGBA8888)]
    (.setColor pixmap Color/BLUE)
    (.fill pixmap)
    (let [texture (Texture. pixmap)]
      (.dispose pixmap)
      texture)))
;; Create its drop-let
(defn create-droplet-image []
  "Create a simple cyan circle texture for raindrops"
  (let [pixmap (com.badlogic.gdx.graphics.Pixmap. RAINDROP-WIDTH RAINDROP-HEIGHT
                                                   com.badlogic.gdx.graphics.Pixmap$Format/RGBA8888)]
    (.setColor pixmap Color/CYAN)
    (.fillCircle pixmap (/ RAINDROP-WIDTH 2) (/ RAINDROP-HEIGHT 2) (/ RAINDROP-WIDTH 2))
    (let [texture (Texture. pixmap)]
      (.dispose pixmap)
      texture)))
;; rain drops
(defn spawn-raindrop! []
  "Spawn a new raindrop at a random x position"
  (let [raindrop (Rectangle.)
        max-x (- SCREEN-WIDTH RAINDROP-WIDTH)
        random-x (* (Math/random) max-x)]
    (set! (.-x raindrop) (float random-x))
    (set! (.-y raindrop) (float SCREEN-HEIGHT))
    (set! (.-width raindrop) (float RAINDROP-WIDTH))
    (set! (.-height raindrop) (float RAINDROP-HEIGHT))
    (.add (:raindrops @game-state) raindrop)
    (swap! game-state assoc :last-drop-time (TimeUtils/nanoTime))))
;; Enable it the bucket to move when it is controlled by the Player
(defn update-bucket! [delta-time]
  "Update bucket position based on input"
  (let [bucket-x (:bucket-x @game-state)]
    (cond
      (or (.isKeyPressed Gdx/input Input$Keys/LEFT)
          (.isKeyPressed Gdx/input Input$Keys/A))
      (swap! game-state update :bucket-x
             #(max 0 (- % (* BUCKET-SPEED delta-time))))

      (or (.isKeyPressed Gdx/input Input$Keys/RIGHT)
          (.isKeyPressed Gdx/input Input$Keys/D))
      (swap! game-state update :bucket-x
             #(min (- SCREEN-WIDTH BUCKET-WIDTH) (+ % (* BUCKET-SPEED delta-time)))))))
;; Enable the Raindrops to drop
(defn update-raindrops! [delta-time]
  "Update raindrop positions and handle collisions"
  (let [{:keys [raindrops bucket-x bucket-y drop-sound]} @game-state
        bucket (Rectangle. bucket-x bucket-y BUCKET-WIDTH BUCKET-HEIGHT)
        iter (.iterator raindrops)]

    ;; Update each raindrop
    (while (.hasNext iter)
      (let [raindrop (.next iter)]
        ;; Move raindrop down
        (set! (.-y raindrop) (- (.-y raindrop) (* RAINDROP-SPEED delta-time)))

        ;; Check collision with bucket
        (if (.overlaps raindrop bucket)
          (do
            (when drop-sound (.play drop-sound))
            (.remove iter)
            (swap! game-state update :score inc))
          ;; Remove if off screen
          (when (< (+ (.-y raindrop) RAINDROP-HEIGHT) 0)
            (.remove iter)))))))
;; Update the raindrop intelligence of movement
(defn should-spawn-raindrop? []
  "Check if enough time has passed to spawn a new raindrop"
  (> (- (TimeUtils/nanoTime) (:last-drop-time @game-state)) 1000000000)) ; 1 second
;; Enable the entire game to evolve or be dynamic
(defn update-game! [delta-time]
  "Main game update loop"
  (update-bucket! delta-time)
  (update-raindrops! delta-time)

  ;; Spawn new raindrops
  (when (should-spawn-raindrop?)
    (spawn-raindrop!))

  ;; Exit game on ESC
  (when (.isKeyPressed Gdx/input Input$Keys/ESCAPE)
    (.exit Gdx/app)))

(defn render-game [batch]
  "Render all game objects"
  (let [{:keys [bucket-x bucket-y raindrops bucket-image droplet-image score]} @game-state]

    ;; Clear screen
    (.glClearColor Gdx/gl 0 0 0.2 1)
    (.glClear Gdx/gl GL20/GL_COLOR_BUFFER_BIT)

    ;; Begin batch
    (.begin batch)

    ;; Draw bucket
    (when bucket-image
      (.draw batch bucket-image bucket-x bucket-y BUCKET-WIDTH BUCKET-HEIGHT))

    ;; Draw raindrops
    (when droplet-image
      (let [iter (.iterator raindrops)]
        (while (.hasNext iter)
          (let [raindrop (.next iter)]
            (.draw batch droplet-image (.-x raindrop) (.-y raindrop) RAINDROP-WIDTH RAINDROP-HEIGHT)))))

    ;; End batch
    (.end batch)))

(defn create-game-screen [batch]
  "Create the main game screen"
  (let [camera (OrthographicCamera.)
        _ (.setToOrtho camera false SCREEN-WIDTH SCREEN-HEIGHT)]

    (proxy [Screen] []
      (show []
        (println "Game screen shown")
        ;; Initialize game assets
        (swap! game-state assoc
               :bucket-image (create-bucket-image)
               :droplet-image (create-droplet-image))

        ;; Try to load sounds (optional, will fail gracefully if files don't exist)
        (try
          (swap! game-state assoc
                 :drop-sound nil  ; We'll use visual feedback only
                 :rain-music nil) ; We'll use visual feedback only
          (catch Exception e
            (println "Could not load audio files (using silent mode):" (.getMessage e))))

        ;; Spawn initial raindrop
        (spawn-raindrop!)
        (println "Game initialized successfully!"))

      (render [delta-time]
        (.update camera)
        (.setProjectionMatrix batch (.combined camera))

        ;; Update game logic
        (update-game! delta-time)

        ;; Render game
        (render-game batch))

      (resize [width height]
        (.setToOrtho camera false width height))

      (pause [])
      (resume [])

      (hide [])

      (dispose []
        (println "Disposing game screen resources...")
        (let [{:keys [bucket-image droplet-image drop-sound rain-music]} @game-state]
          (when bucket-image (.dispose bucket-image))
          (when droplet-image (.dispose droplet-image))
          (when drop-sound (.dispose drop-sound))
          (when rain-music (.dispose rain-music)))))))

(comment
  ;; REPL helpers for development
  @game-state
  (spawn-raindrop!)
  )