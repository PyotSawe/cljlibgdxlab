(ns drop-game.main-physics
  "Enhanced Drop Game with Box2D Physics Integration"
  (:gen-class)
  (:require [drop-game.physics :as physics])
  (:import [com.badlogic.gdx ApplicationListener Gdx Input$Keys]
           [com.badlogic.gdx.backends.lwjgl3 Lwjgl3Application Lwjgl3ApplicationConfiguration]
           [com.badlogic.gdx.graphics Texture Color]
           [com.badlogic.gdx.graphics.g2d Sprite SpriteBatch BitmapFont GlyphLayout]
           [com.badlogic.gdx.audio Sound Music]
           [com.badlogic.gdx.math MathUtils Rectangle Vector2]
           [com.badlogic.gdx.utils Array ScreenUtils]
           [com.badlogic.gdx.utils.viewport FitViewport ScreenViewport]))

;; Assets
(def ^Texture background-texture (atom nil))
(def ^Texture bucket-texture (atom nil))
(def ^Texture drop-texture (atom nil))
(def ^Sound drop-sound (atom nil))
(def ^Music music (atom nil))

;; Rendering objects
(def ^SpriteBatch sprite-batch (atom nil))
(def ^FitViewport viewport (atom nil))
(def ^ScreenViewport ui-viewport (atom nil))

;; Text rendering components
(def ^BitmapFont title-font (atom nil))
(def ^BitmapFont score-font (atom nil))
(def ^BitmapFont ui-font (atom nil))
(def ^GlyphLayout glyph-layout (atom nil))

;; Game objects
(def ^Sprite bucket-sprite (atom nil))
(def ^Vector2 touch-pos (atom nil))
(def ^Array drop-sprites (atom nil)) ; For visual representation
(def drop-timer (atom 0.0))

;; Game state with creative UI elements
(def score (atom 0))
(def high-score (atom 0))
(def drops-caught (atom 0))
(def drops-missed (atom 0))
(def game-time (atom 0.0))
(def score-multiplier (atom 1))
(def last-score-time (atom 0.0))
(def combo-count (atom 0))
(def show-combo (atom false))
(def combo-timer (atom 0.0))

;; Navigation button state management
(def button-states (atom {:left false :right false :pause false :restart false :physics false :wind-left false :wind-right false}))
(def button-timers (atom {:left 0.0 :right 0.0 :pause 0.0 :restart 0.0 :physics 0.0 :wind-left 0.0 :wind-right 0.0}))
(def game-paused (atom false))
(def show-physics-debug (atom false))
(def button-press-duration 0.2)

;; Physics collision handling
(defn handle-physics-collision [collision-type droplet-body]
  "Handle physics collision callbacks"
  (case collision-type
    :caught (do
              (when @drop-sound (.play @drop-sound))
              (let [points (* 10 @score-multiplier)]
                (swap! score + points)
                (swap! drops-caught inc)
                (swap! combo-count inc)
                (reset! last-score-time @game-time)
                
                ;; Combo system
                (when (>= @combo-count 3)
                  (reset! show-combo true)
                  (reset! combo-timer 2.0)
                  (when (>= @combo-count 5)
                    (swap! score-multiplier inc)))
                
                ;; Update high score
                (when (> @score @high-score)
                  (reset! high-score @score))
                
                ;; Celebration messages
                (cond
                  (>= @combo-count 10) (println "🔥 AMAZING COMBO x" @combo-count "!")
                  (>= @combo-count 5) (println "⭐ GREAT COMBO x" @combo-count "!")
                  (= (mod @score 100) 0) (println "🏆 Score milestone:" @score))))
    
    :missed (do
              (swap! drops-missed inc)
              (reset! combo-count 0)
              (reset! score-multiplier 1)
              (reset! show-combo false))
    
    :default nil))

(defn create-fonts []
  "Create fonts for the UI using default BitmapFont"
  (try
    (let [font1 (BitmapFont.)
          font2 (BitmapFont.) 
          font3 (BitmapFont.)]
      (reset! title-font font1)
      (reset! score-font font2) 
      (reset! ui-font font3)
      (println "Default fonts created successfully!"))
    (catch Exception e
      (println "Error creating fonts:" (.getMessage e))
      (let [fallback-font (BitmapFont.)]
        (reset! ui-font fallback-font)
        (reset! score-font fallback-font)
        (reset! title-font fallback-font)))))

(defn highlight-button [button-key]
  "Highlight a button when pressed"
  (swap! button-states assoc button-key true)
  (swap! button-timers assoc button-key button-press-duration))

(defn update-button-states [delta-time]
  "Update button highlighting timers"
  (doseq [button-key (keys @button-timers)]
    (let [current-timer (get @button-timers button-key)]
      (if (> current-timer 0)
        (do
          (swap! button-timers assoc button-key (- current-timer delta-time))
          (when (<= (get @button-timers button-key) 0)
            (swap! button-states assoc button-key false)))))))

(defn create-droplet []
  "Create both visual sprite and physics body for droplet"
  (let [drop-width 1
        drop-height 1
        world-width (.getWorldWidth @viewport)
        world-height (.getWorldHeight @viewport)
        x (+ (* (Math/random) (- world-width drop-width)) (/ drop-width 2))
        y world-height
        
        ;; Create visual sprite
        drop-sprite (Sprite. @drop-texture)]
    (.setSize drop-sprite drop-width drop-height)
    (.setX drop-sprite (float x))
    (.setY drop-sprite (float y))
    (.add @drop-sprites drop-sprite)
    
    ;; Create physics body
    (physics/create-droplet-body (* x 100) (* y 100) 32)))

(defn input []
  "Enhanced input handling with physics integration"
  (let [speed 4
        delta (.getDeltaTime Gdx/graphics)]

    ;; Update button highlight timers
    (update-button-states delta)

    ;; Exit game with ESC key
    (when (.isKeyPressed Gdx/input Input$Keys/ESCAPE)
      (println (str "Game Over! Final Score: " @score))
      (.exit Gdx/app))

    ;; Toggle physics debug with P key
    (when (.isKeyJustPressed Gdx/input Input$Keys/P)
      (highlight-button :physics)
      (swap! show-physics-debug not)
      (println (str "Physics debug " (if @show-physics-debug "enabled" "disabled"))))

    ;; Wind effects with Q and E keys
    (when (.isKeyPressed Gdx/input Input$Keys/Q)
      (highlight-button :wind-left)
      (physics/add-wind-force -1.0))
    
    (when (.isKeyPressed Gdx/input Input$Keys/E)
      (highlight-button :wind-right)
      (physics/add-wind-force 1.0))

    ;; Pause/Resume with SPACE key
    (when (.isKeyJustPressed Gdx/input Input$Keys/SPACE)
      (highlight-button :pause)
      (swap! game-paused not)
      (println (if @game-paused "Game Paused" "Game Resumed")))

    ;; Restart with R key
    (when (.isKeyJustPressed Gdx/input Input$Keys/R)
      (highlight-button :restart)
      (reset! score 0)
      (reset! drops-caught 0)
      (reset! drops-missed 0)
      (reset! game-time 0.0)
      (reset! combo-count 0)
      (reset! score-multiplier 1)
      (reset! game-paused false)
      (.clear @drop-sprites)
      ;; Clear physics droplets
      (doseq [body @physics/droplet-bodies]
        (.destroyBody @physics/physics-world body))
      (reset! physics/droplet-bodies [])
      (println "Game Restarted!"))

    ;; Only allow movement if game is not paused
    (when (not @game-paused)
      ;; Keyboard controls with physics integration
      (when (or (.isKeyPressed Gdx/input Input$Keys/RIGHT)
                (.isKeyPressed Gdx/input Input$Keys/D))
        (highlight-button :right)
        (.translateX @bucket-sprite (* speed delta))
        (when @physics/bucket-body
          (let [[current-x current-y] (physics/get-bucket-position)]
            (physics/update-bucket-position (+ current-x (* speed delta 100)) current-y))))

      (when (or (.isKeyPressed Gdx/input Input$Keys/LEFT)
                (.isKeyPressed Gdx/input Input$Keys/A))
        (highlight-button :left)
        (.translateX @bucket-sprite (* (- speed) delta))
        (when @physics/bucket-body
          (let [[current-x current-y] (physics/get-bucket-position)]
            (physics/update-bucket-position (- current-x (* speed delta 100)) current-y))))

      ;; Mouse/touch controls
      (when (.isTouched Gdx/input)
        (.set @touch-pos (.getX Gdx/input) (.getY Gdx/input))
        (.unproject @viewport @touch-pos)
        (.setCenterX @bucket-sprite (.x @touch-pos))
        (when @physics/bucket-body
          (physics/update-bucket-position 
            (* (.x @touch-pos) 100) 
            (* (.getY @bucket-sprite) 100)))))))

(defn logic []
  "Enhanced game logic with physics simulation"
  (let [world-width (.getWorldWidth @viewport)
        world-height (.getWorldHeight @viewport)
        bucket-width (.getWidth @bucket-sprite)
        bucket-height (.getHeight @bucket-sprite)
        delta (.getDeltaTime Gdx/graphics)]

    ;; Always update game time
    (swap! game-time + delta)
    
    ;; Update combo timer
    (when @show-combo
      (swap! combo-timer - delta)
      (when (<= @combo-timer 0)
        (reset! show-combo false)))

    ;; Only update game logic if not paused
    (when (not @game-paused)
      ;; Update physics world
      (physics/update-physics-world delta)
      
      ;; Synchronize visual sprites with physics bodies
      (let [physics-positions (physics/get-droplet-positions)]
        ;; Update existing sprite positions based on physics
        (doseq [i (range (.-size @drop-sprites))]
          (when (< i (count physics-positions))
            (let [drop-sprite (.get @drop-sprites i)
                  physics-pos (nth physics-positions i)]
              (.setPosition drop-sprite 
                           (/ (:x physics-pos) 100.0)
                           (/ (:y physics-pos) 100.0))))))
      
      ;; Clamp bucket movement to viewport bounds
      (.setX @bucket-sprite
             (MathUtils/clamp (.getX @bucket-sprite) 0.0 (- world-width bucket-width)))

      ;; Handle droplet collection from physics collision callbacks
      ;; This will be handled in the physics contact listener
      
      ;; Spawn new droplets with timer
      (let [current-timer @drop-timer
            new-timer (+ current-timer delta)]
        (if (> new-timer 1)
          (do
            (reset! drop-timer 0)
            (create-droplet))
          (reset! drop-timer new-timer))))))

(defn format-time [seconds]
  "Format time as MM:SS"
  (let [minutes (int (/ seconds 60))
        secs (int (mod seconds 60))]
    (format "%02d:%02d" minutes secs)))

(defn draw-physics-controls [screen-width screen-height margin]
  "Draw physics-related control buttons"
  (let [button-y (+ margin 100)
        physics-button-x margin
        wind-left-x (+ margin 120)
        wind-right-x (+ margin 200)]
    
    ;; Physics debug toggle
    (let [highlighted? (:physics @button-states)]
      (if highlighted?
        (.setColor @ui-font Color/YELLOW)
        (.setColor @ui-font (if @show-physics-debug Color/GREEN Color/WHITE)))
      (.draw @ui-font @sprite-batch 
             (str "P: Physics " (if @show-physics-debug "ON" "OFF"))
             physics-button-x button-y))
    
    ;; Wind controls
    (let [wind-left-highlighted? (:wind-left @button-states)]
      (if wind-left-highlighted?
        (.setColor @ui-font Color/YELLOW)
        (.setColor @ui-font Color/WHITE))
      (.draw @ui-font @sprite-batch "Q: Wind ◀" wind-left-x button-y))
    
    (let [wind-right-highlighted? (:wind-right @button-states)]
      (if wind-right-highlighted?
        (.setColor @ui-font Color/YELLOW)
        (.setColor @ui-font Color/WHITE))
      (.draw @ui-font @sprite-batch "E: Wind ▶" wind-right-x button-y))))

(defn draw-navigation-buttons [screen-width screen-height margin]
  "Draw interactive navigation buttons with highlighting"
  (let [button-y (+ margin 60)]
    
    ;; Movement buttons
    (doseq [[button-key x label] [[:left margin "◀ LEFT (A)"]
                                  [:right (+ margin 100) "RIGHT (D) ▶"] 
                                  [:pause (+ margin 220) (if @game-paused "▶ PLAY" "⏸ PAUSE")]
                                  [:restart (+ margin 320) "🔄 RESTART (R)"]]]
      (let [highlighted? (get @button-states button-key false)]
        (if highlighted?
          (.setColor @ui-font Color/YELLOW)
          (.setColor @ui-font Color/WHITE))
        (.draw @ui-font @sprite-batch label x button-y)))
    
    ;; Physics controls
    (draw-physics-controls screen-width screen-height margin)
    
    ;; Instructions
    (.setColor @ui-font Color/LIGHT_GRAY)
    (.draw @ui-font @sprite-batch 
           "Movement: A/D | Physics: P | Wind: Q/E | Pause: SPACE | Restart: R | Exit: ESC"
           margin (+ button-y 40))))

(defn draw-ui []
  "Enhanced UI with physics information"
  (when (and @ui-viewport @score-font @title-font @ui-font @glyph-layout)
    (try
      (.apply @ui-viewport)
      (.setProjectionMatrix @sprite-batch (.combined (.getCamera @ui-viewport)))
      
      (.begin @sprite-batch)
      
      (let [screen-width (.getWorldWidth @ui-viewport)
            screen-height (.getWorldHeight @ui-viewport)
            margin 20
            line-height 25]
        
        ;; Title
        (.setColor @title-font Color/YELLOW)
        (.draw @title-font @sprite-batch 
               "🌧️ BOX2D PHYSICS DROP GAME 🪣⚛️" 
               (/ screen-width 6) (- screen-height margin))
        
        ;; Pause overlay
        (when @game-paused
          (.setColor @title-font Color/RED)
          (.draw @title-font @sprite-batch 
                 "⏸ GAME PAUSED ⏸"
                 (/ screen-width 3) (/ screen-height 2)))
        
        ;; Score display
        (.setColor @score-font Color/CYAN)
        (.draw @score-font @sprite-batch 
               (str "SCORE: " @score) 
               margin (- screen-height (* 2 line-height)))
        
        (.setColor @score-font Color/GOLD)
        (.draw @score-font @sprite-batch 
               (str "HIGH: " @high-score) 
               margin (- screen-height (* 3 line-height)))
        
        ;; Stats
        (.setColor @ui-font Color/WHITE)
        (.draw @ui-font @sprite-batch 
               (str "Time: " (format-time @game-time)) 
               (- screen-width 150) (- screen-height (* 2 line-height)))
        
        (.draw @ui-font @sprite-batch 
               (str "Caught: " @drops-caught) 
               (- screen-width 150) (- screen-height (* 3 line-height)))
        
        (.draw @ui-font @sprite-batch 
               (str "Physics Bodies: " (count @physics/droplet-bodies)) 
               (- screen-width 150) (- screen-height (* 4 line-height)))
        
        ;; Multiplier and combo
        (when (> @score-multiplier 1)
          (.setColor @score-font Color/ORANGE)
          (.draw @score-font @sprite-batch 
                 (str "MULTIPLIER x" @score-multiplier) 
                 (- screen-width 200) (- screen-height (* 5 line-height))))
        
        (when @show-combo
          (.setColor @title-font Color/RED)
          (.draw @title-font @sprite-batch 
                 (str "🔥 COMBO x" @combo-count " 🔥")
                 (/ screen-width 3) (/ screen-height 2)))
        
        ;; Navigation buttons
        (draw-navigation-buttons screen-width screen-height margin)
        
        ;; Accuracy
        (when (> (+ @drops-caught @drops-missed) 0)
          (let [accuracy (/ (* 100.0 @drops-caught) (+ @drops-caught @drops-missed))]
            (.setColor @ui-font 
                       (cond 
                         (>= accuracy 90) Color/GREEN
                         (>= accuracy 70) Color/YELLOW
                         :else Color/RED))
            (.draw @ui-font @sprite-batch 
                   (str "Accuracy: " (format "%.1f%%" accuracy)) 
                   margin margin))))
      
      (.end @sprite-batch)
      (catch Exception e
        (println "Error rendering UI:" (.getMessage e))
        (.end @sprite-batch)))))

(defn draw []
  "Enhanced rendering with physics debug"
  (let [world-width (.getWorldWidth @viewport)
        world-height (.getWorldHeight @viewport)]

    ;; Clear screen
    (ScreenUtils/clear 0.1 0.3 0.8 1.0)
    (.apply @viewport)
    (.setProjectionMatrix @sprite-batch (.combined (.getCamera @viewport)))

    ;; Draw game world
    (.begin @sprite-batch)
    (.draw @sprite-batch @background-texture (float 0) (float 0) (float world-width) (float world-height))
    (.draw @bucket-sprite @sprite-batch)
    
    ;; Draw droplet sprites (synced with physics)
    (let [iter (.iterator @drop-sprites)]
      (while (.hasNext iter)
        (let [drop-sprite (.next iter)]
          (.draw drop-sprite @sprite-batch))))
    
    (.end @sprite-batch)
    
    ;; Draw physics debug visualization
    (when @show-physics-debug
      (physics/render-physics-debug (.getCamera @viewport)))
    
    ;; Draw UI overlay
    (draw-ui)))

(defn create-physics-app []
  "Create the enhanced application with Box2D physics"
  (proxy [ApplicationListener] []

    (create []
      (println "Initializing Enhanced Drop Game with Box2D Physics...")

      ;; Load assets
      (reset! background-texture (Texture. "background.png"))
      (reset! bucket-texture (Texture. "bucket.png"))
      (reset! drop-texture (Texture. "drop.png"))

      ;; Load audio
      (try
        (reset! drop-sound (.newSound Gdx/audio (.internal Gdx/files "drop.mp3")))
        (catch Exception _ (println "Could not load drop.mp3")))

      (try
        (reset! music (.newMusic Gdx/audio (.internal Gdx/files "music.mp3")))
        (catch Exception _ (println "Could not load music.mp3")))

      ;; Initialize rendering
      (reset! sprite-batch (SpriteBatch.))
      (reset! viewport (FitViewport. 8 5))
      (reset! ui-viewport (ScreenViewport.))
      (reset! glyph-layout (GlyphLayout.))
      (create-fonts)

      ;; Initialize game objects
      (reset! bucket-sprite (Sprite. @bucket-texture))
      (.setSize @bucket-sprite 1 1)
      (.setPosition @bucket-sprite 3.5 0.5)

      (reset! touch-pos (Vector2.))
      (reset! drop-sprites (Array.))

      ;; Initialize Box2D physics
      (physics/create-physics-world)
      (physics/set-collision-callback handle-physics-collision)
      (physics/create-world-boundaries)
      (physics/create-bucket-body 350 50 64 64)

      ;; Setup music
      (when @music
        (.setLooping @music true)
        (.setVolume @music 0.3)
        (.play @music))

      (println "\n=== BOX2D ENHANCED DROP GAME ===")
      (println "Enhanced Controls:")
      (println "  A/D or ←/→ - Move bucket")
      (println "  SPACE - Pause/Resume")
      (println "  R - Restart game")
      (println "  P - Toggle physics debug visualization")
      (println "  Q/E - Apply wind force left/right")
      (println "  ESC - Exit")
      (println "🎯 Realistic physics simulation with Box2D!")
      (println "=====================================\n")

      (println "Physics-enhanced game initialized!"))

    (resize [width height]
      (.update @viewport width height true)
      (.update @ui-viewport width height true))

    (render []
      (input)
      (logic)
      (draw))

    (pause [])
    (resume [])

    (dispose []
      (println "Disposing physics-enhanced game resources...")
      (physics/dispose-physics-world)
      (when @background-texture (.dispose @background-texture))
      (when @bucket-texture (.dispose @bucket-texture))
      (when @drop-texture (.dispose @drop-texture))
      (when @drop-sound (.dispose @drop-sound))
      (when @music (.dispose @music))
      (when @sprite-batch (.dispose @sprite-batch)))))

(defn get-physics-configuration []
  "Create configuration for physics-enhanced game"
  (doto (Lwjgl3ApplicationConfiguration.)
    (.setTitle "Enhanced Drop Game with Box2D Physics - P: Debug | Q/E: Wind")
    (.setWindowedMode 800 500)
    (.setResizable false)))

(defn -main [& _args]
  "Main entry point for physics-enhanced game"
  (println "Starting Box2D Physics Enhanced Drop Game...")
  (try
    (let [config (get-physics-configuration)
          app-listener (create-physics-app)]
      (Lwjgl3Application. app-listener config))
    (catch Exception e
      (println "Error starting physics game:" (.getMessage e))
      (.printStackTrace e)
      (System/exit 1))))

(comment
  ;; Development helpers
  (-main)
  @physics/physics-world)