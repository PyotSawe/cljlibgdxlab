(ns drop-game.core
  (:gen-class)
  (:import [com.badlogic.gdx ApplicationListener Gdx Input$Keys]
           [com.badlogic.gdx.backends.lwjgl3 Lwjgl3Application Lwjgl3ApplicationConfiguration]
           [com.badlogic.gdx.graphics Texture Color]
           [com.badlogic.gdx.graphics.g2d Sprite SpriteBatch BitmapFont]
           [com.badlogic.gdx.audio Sound Music]
           [com.badlogic.gdx.math MathUtils Rectangle Vector2]
           [com.badlogic.gdx.utils Array ScreenUtils]
           [com.badlogic.gdx.utils.viewport FitViewport]))

;; Game state atom to hold all game data
(def game-state (atom {:drop-timer 0.0 :score 0 :game-speed 1.0 :lives 3}))

;; Assets and rendering objects
(def ^Texture background-texture (atom nil))
(def ^Texture bucket-texture (atom nil))
(def ^Texture drop-texture (atom nil))
(def ^Sound drop-sound (atom nil))
(def ^Music music (atom nil))
(def ^SpriteBatch sprite-batch (atom nil))
(def ^FitViewport viewport (atom nil))
(def ^BitmapFont font (atom nil))

;; Game objects
(def ^Sprite bucket-sprite (atom nil))
(def ^Vector2 touch-pos (atom nil))
(def ^Array drop-sprites (atom nil))

;; Collision rectangles
(def ^Rectangle bucket-rectangle (atom nil))
(def ^Rectangle drop-rectangle (atom nil))

(defn create-droplet []
  "Create a new droplet sprite"
  (let [drop-width 1
        drop-height 1
        world-width (.getWorldWidth @viewport)
        world-height (.getWorldHeight @viewport)
        drop-sprite (Sprite. @drop-texture)]
    (.setSize drop-sprite drop-width drop-height)
    (.setX drop-sprite (float (* (Math/random) (- world-width drop-width))))
    (.setY drop-sprite world-height)
    (.add @drop-sprites drop-sprite)))

(defn input []
  "Handle user input with improved controls"
  (let [speed (* 4 (:game-speed @game-state))
        delta (.getDeltaTime Gdx/graphics)]

    ;; Exit on ESC
    (when (.isKeyPressed Gdx/input Input$Keys/ESCAPE)
      (.exit Gdx/app))

    ;; Keyboard controls (WASD and arrows)
    (when (or (.isKeyPressed Gdx/input Input$Keys/RIGHT)
              (.isKeyPressed Gdx/input Input$Keys/D))
      (.translateX @bucket-sprite (* speed delta)))

    (when (or (.isKeyPressed Gdx/input Input$Keys/LEFT)
              (.isKeyPressed Gdx/input Input$Keys/A))
      (.translateX @bucket-sprite (* (- speed) delta)))

    ;; Mouse/touch controls
    (when (.isTouched Gdx/input)
      (.set @touch-pos (.getX Gdx/input) (.getY Gdx/input))
      (.unproject @viewport @touch-pos)
      (.setCenterX @bucket-sprite (.x @touch-pos)))))

(defn logic []
  "Game logic update with improved features"
  (let [world-width (.getWorldWidth @viewport)
        world-height (.getWorldHeight @viewport)
        bucket-width (.getWidth @bucket-sprite)
        bucket-height (.getHeight @bucket-sprite)
        delta (.getDeltaTime Gdx/graphics)
        current-state @game-state]

    ;; Clamp bucket movement to viewport bounds
    (.setX @bucket-sprite
           (MathUtils/clamp (.getX @bucket-sprite) 0.0 (- world-width bucket-width)))

    ;; Update bucket rectangle for collision detection
    (.set @bucket-rectangle (.getX @bucket-sprite) (.getY @bucket-sprite)
          bucket-width bucket-height)

    ;; Update droplets - loop backwards for safe removal
    (loop [i (dec (.-size @drop-sprites))]
      (when (>= i 0)
        (let [drop-sprite (.get @drop-sprites i)
              drop-width (.getWidth drop-sprite)
              drop-height (.getHeight drop-sprite)]

          ;; Move droplet down faster based on game speed
          (.translateY drop-sprite (* -2 delta (:game-speed current-state)))

          ;; Update drop rectangle for collision
          (.set @drop-rectangle (.getX drop-sprite) (.getY drop-sprite)
                drop-width drop-height)

          ;; Check if droplet fell off screen
          (if (< (.getY drop-sprite) (- drop-height))
            (do
              (.removeIndex @drop-sprites i)
              ;; Lose a life when missing a droplet
              (swap! game-state update :lives dec)
              (when (<= (:lives @game-state) 0)
                (println (str "Game Over! Final Score: " (:score @game-state)))
                (Thread/sleep 2000)
                (.exit Gdx/app)))
            ;; Check collision with bucket
            (when (.overlaps @bucket-rectangle @drop-rectangle)
              (.removeIndex @drop-sprites i)
              (when @drop-sound (.play @drop-sound))
              ;; Increase score and speed up game
              (swap! game-state update :score inc)
              (swap! game-state update :game-speed + 0.01)))

          (recur (dec i)))))

    ;; Spawn new droplets with increasing frequency
    (let [current-timer (:drop-timer current-state)
          spawn-interval (/ 1.0 (:game-speed current-state))
          new-timer (+ current-timer delta)]
      (if (> new-timer spawn-interval)
        (do
          (swap! game-state assoc :drop-timer 0)
          (create-droplet))
        (swap! game-state assoc :drop-timer new-timer)))))

(defn draw []
  "Render the game with UI improvements"
  (let [world-width (.getWorldWidth @viewport)
        world-height (.getWorldHeight @viewport)
        current-state @game-state]

    ;; Clear screen with a nice blue background
    (ScreenUtils/clear 0.1 0.3 0.8 1.0)
    (.apply @viewport)
    (.setProjectionMatrix @sprite-batch (.combined (.getCamera @viewport)))

    ;; Begin drawing
    (.begin @sprite-batch)

    ;; Draw background
    (.draw @sprite-batch @background-texture (float 0) (float 0) (float world-width) (float world-height))

    ;; Draw bucket
    (.draw @bucket-sprite @sprite-batch)

    ;; Draw all droplets
    (let [iter (.iterator @drop-sprites)]
      (while (.hasNext iter)
        (let [drop-sprite (.next iter)]
          (.draw drop-sprite @sprite-batch))))

    ;; Draw UI text - use proper scaling for world coordinates
    (let [scale 0.003] ; Scale factor for font in world coordinates
      (.getData @font) ; Access font data for scaling
      (.setScale (.getData @font) scale scale)
      (.draw @font @sprite-batch (str "Score: " (:score current-state)) 0.2 (- world-height 0.2))
      (.draw @font @sprite-batch (str "Lives: " (:lives current-state)) 0.2 (- world-height 0.6))
      (.draw @font @sprite-batch (str "Speed: " (format "%.1f" (:game-speed current-state))) 0.2 (- world-height 1.0))
      (.draw @font @sprite-batch "WASD/Arrows to move, ESC to exit" 0.2 0.2))

    ;; End drawing
    (.end @sprite-batch)))

(defn create-app-listener []
  "Create the main ApplicationListener"
  (proxy [ApplicationListener] []

    (create []
      (println "Initializing Enhanced Drop Game...")

      ;; Load assets
      (reset! background-texture (Texture. "background.png"))
      (reset! bucket-texture (Texture. "bucket.png"))
      (reset! drop-texture (Texture. "drop.png"))

      ;; Load audio with error handling
      (try
        (reset! drop-sound (.newSound Gdx/audio (.internal Gdx/files "drop.mp3")))
        (catch Exception _
          (println "Could not load drop.mp3 - continuing without sound")))

      (try
        (reset! music (.newMusic Gdx/audio (.internal Gdx/files "music.mp3")))
        (catch Exception _
          (println "Could not load music.mp3 - continuing without music")))

      ;; Initialize rendering objects
      (reset! sprite-batch (SpriteBatch.))
      (reset! viewport (FitViewport. 8 5))
      (reset! font (BitmapFont.))
      (.setColor @font Color/WHITE)

      ;; Initialize game objects
      (reset! bucket-sprite (Sprite. @bucket-texture))
      (.setSize @bucket-sprite 1 1)
      (.setPosition @bucket-sprite 3.5 0.5) ; Start in center-bottom

      (reset! touch-pos (Vector2.))
      (reset! drop-sprites (Array.))
      (reset! bucket-rectangle (Rectangle.))
      (reset! drop-rectangle (Rectangle.))

      ;; Setup and play music
      (when @music
        (.setLooping @music true)
        (.setVolume @music 0.3)
        (.play @music))

      (println "Enhanced game initialized successfully!"))

    (resize [width height]
      (.update @viewport width height true))

    (render []
      (input)
      (logic)
      (draw))

    (pause [])
    (resume [])

    (dispose []
      (println "Disposing game resources...")
      (when @background-texture (.dispose @background-texture))
      (when @bucket-texture (.dispose @bucket-texture))
      (when @drop-texture (.dispose @drop-texture))
      (when @drop-sound (.dispose @drop-sound))
      (when @music (.dispose @music))
      (when @sprite-batch (.dispose @sprite-batch))
      (when @font (.dispose @font)))))

(defn create-config []
  "Create LibGDX application configuration"
  (doto (Lwjgl3ApplicationConfiguration.)
    (.setTitle "Enhanced Drop Game - Clojure + LibGDX")
    (.setWindowedMode 800 500)
    (.setResizable false)))

(defn -main [& _args]
  (println "Starting Enhanced LibGDX Drop Game...")
  (try
    (let [config (create-config)
          app-listener (create-app-listener)]
      (Lwjgl3Application. app-listener config))
    (catch Exception e
      (println "Error starting game:" (.getMessage e))
      (.printStackTrace e)
      (System/exit 1))))

(comment
  ;; Development helpers
  (-main)
  @game-state)