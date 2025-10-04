(ns drop-game.main
  "Drop Game - Enhanced version with Box2D Physics"
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

;; Assets - exactly as in Java tutorial
(def ^Texture background-texture (atom nil))
(def ^Texture bucket-texture (atom nil))
(def ^Texture drop-texture (atom nil))
(def ^Sound drop-sound (atom nil))
(def ^Music music (atom nil))

;; Boilerplate rendering objects
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
(def ^Array drop-sprites (atom nil))
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
(def button-states (atom {:left false :right false :pause false :restart false}))
(def button-timers (atom {:left 0.0 :right 0.0 :pause 0.0 :restart 0.0}))
(def game-paused (atom false))
(def button-press-duration 0.2) ; How long buttons stay highlighted

;; Collision detection rectangles
(def ^Rectangle bucket-rectangle (atom nil))
(def ^Rectangle drop-rectangle (atom nil))

(defn create-fonts []
  "Create fonts for the UI using default BitmapFont"
  (try
    ;; Create default fonts 
    (let [font1 (BitmapFont.)
          font2 (BitmapFont.) 
          font3 (BitmapFont.)]
      
      (reset! title-font font1)
      (reset! score-font font2) 
      (reset! ui-font font3)
      
      (println "Default fonts created successfully!"))
    (catch Exception e
      (println "Error creating fonts:" (.getMessage e))
      ;; Fallback to single shared font
      (let [fallback-font (BitmapFont.)]
        (reset! ui-font fallback-font)
        (reset! score-font fallback-font)
        (reset! title-font fallback-font)))))

(defn create-droplet []
  "Create a new droplet sprite - matches Java tutorial createDroplet()"
  (let [drop-width 1
        drop-height 1
        world-width (.getWorldWidth @viewport)
        world-height (.getWorldHeight @viewport)
        drop-sprite (Sprite. @drop-texture)]
    (.setSize drop-sprite drop-width drop-height)
    (.setX drop-sprite (float (* (Math/random) (- world-width drop-width))))
    (.setY drop-sprite world-height)
    (.add @drop-sprites drop-sprite)))

(defn highlight-button [button-key]
  "Highlight a button when pressed"
  (swap! button-states assoc button-key true)
  (swap! button-timers assoc button-key button-press-duration))

(defn update-button-states [delta-time]
  "Update button highlighting timers"
  (doseq [button-key [:left :right :pause :restart]]
    (let [current-timer (get @button-timers button-key)]
      (if (> current-timer 0)
        (do
          (swap! button-timers assoc button-key (- current-timer delta-time))
          (when (<= (get @button-timers button-key) 0)
            (swap! button-states assoc button-key false)))))))

(defn input []
  "Enhanced input handling with button highlighting"
  (let [speed 4
        delta (.getDeltaTime Gdx/graphics)]

    ;; Update button highlight timers
    (update-button-states delta)

    ;; Exit game with ESC key
    (when (.isKeyPressed Gdx/input Input$Keys/ESCAPE)
      (println (str "Game Over! Final Score: " @score))
      (.exit Gdx/app))

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
      (println "Game Restarted!"))

    ;; Only allow movement if game is not paused
    (when (not @game-paused)
      ;; Keyboard controls - Arrow keys and WASD with highlighting
      (when (or (.isKeyPressed Gdx/input Input$Keys/RIGHT)
                (.isKeyPressed Gdx/input Input$Keys/D))
        (when (not (:right @button-states))
          (highlight-button :right))
        (.translateX @bucket-sprite (* speed delta)))

      (when (or (.isKeyPressed Gdx/input Input$Keys/LEFT)
                (.isKeyPressed Gdx/input Input$Keys/A))
        (when (not (:left @button-states))
          (highlight-button :left))
        (.translateX @bucket-sprite (* (- speed) delta)))

      ;; Mouse/touch controls
      (when (.isTouched Gdx/input)
        (.set @touch-pos (.getX Gdx/input) (.getY Gdx/input))
        (.unproject @viewport @touch-pos)
        (.setCenterX @bucket-sprite (.x @touch-pos)))))))

(defn logic []
  "Game logic update - matches Java tutorial logic() method"
  (let [world-width (.getWorldWidth @viewport)
        world-height (.getWorldHeight @viewport)
        bucket-width (.getWidth @bucket-sprite)
        bucket-height (.getHeight @bucket-sprite)
        delta (.getDeltaTime Gdx/graphics)]

    ;; Always update game time (for UI animations)
    (swap! game-time + delta)
    
    ;; Update combo timer
    (when @show-combo
      (swap! combo-timer - delta)
      (when (<= @combo-timer 0)
        (reset! show-combo false)))

    ;; Only update game logic if not paused
    (when (not @game-paused)
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

            ;; Move droplet down
            (.translateY drop-sprite (* -2 delta))

            ;; Update drop rectangle for collision
            (.set @drop-rectangle (.getX drop-sprite) (.getY drop-sprite)
                  drop-width drop-height)

            ;; Check if droplet fell off screen
            (if (< (.getY drop-sprite) (- drop-height))
              (do
                (.removeIndex @drop-sprites i)
                ;; Miss penalty - reset combo
                (swap! drops-missed inc)
                (reset! combo-count 0)
                (reset! score-multiplier 1)
                (reset! show-combo false))
              ;; Check collision with bucket
              (when (.overlaps @bucket-rectangle @drop-rectangle)
                (.removeIndex @drop-sprites i)
                (when @drop-sound (.play @drop-sound))
                ;; Creative scoring system
                (let [current-time @game-time
                      time-since-last (- current-time @last-score-time)
                      points (* 10 @score-multiplier)]
                  (swap! score + points)
                  (swap! drops-caught inc)
                  (swap! combo-count inc)
                  (reset! last-score-time current-time)
                  
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
                    (= (mod @score 100) 0) (println "🏆 Score milestone:" @score)))))

            (recur (dec i)))))

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

(defn draw-navigation-buttons [screen-width screen-height margin]
  "Draw interactive navigation buttons with highlighting"
  (let [button-width 80
        button-height 30
        button-y (+ margin 60)
        left-button-x margin
        right-button-x (+ margin button-width 10)
        pause-button-x (+ margin (* 2 button-width) 20)
        restart-button-x (+ margin (* 3 button-width) 30)]
    
    ;; Draw button backgrounds and labels
    (doseq [[button-key x label] [[:left left-button-x "◀ LEFT"]
                                  [:right right-button-x "RIGHT ▶"] 
                                  [:pause pause-button-x (if @game-paused "▶ PLAY" "⏸ PAUSE")]
                                  [:restart restart-button-x "🔄 RESTART"]]]
      (let [highlighted? (get @button-states button-key false)]
        ;; Set button color based on highlight state
        (if highlighted?
          (.setColor @ui-font Color/YELLOW)  ; Highlighted color
          (.setColor @ui-font Color/WHITE))  ; Normal color
        
        ;; Draw button label
        (.draw @ui-font @sprite-batch label x button-y)))
    
    ;; Draw button instructions
    (.setColor @ui-font Color/LIGHT_GRAY)
    (.draw @ui-font @sprite-batch 
           "Keys: A/D or ←/→ | SPACE: Pause | R: Restart | ESC: Exit"
           margin (+ button-y 35))))

(defn draw-ui []
  "Draw creative UI elements with scores and stats"
  (when (and @ui-viewport @score-font @title-font @ui-font @glyph-layout)
    (try
      (.apply @ui-viewport)
      (.setProjectionMatrix @sprite-batch (.combined (.getCamera @ui-viewport)))
      
      (.begin @sprite-batch)
      
      (let [screen-width (.getWorldWidth @ui-viewport)
            screen-height (.getWorldHeight @ui-viewport)
            margin 20
            line-height 25]
        
        ;; Title at top center - simplified
        (.setColor @title-font Color/YELLOW)
        (.draw @title-font @sprite-batch 
               "🌧️ ENHANCED DROP GAME 🪣" 
               (/ screen-width 4) (- screen-height margin))
        
        ;; Pause overlay
        (when @game-paused
          (.setColor @title-font Color/RED)
          (.draw @title-font @sprite-batch 
                 "⏸ GAME PAUSED ⏸"
                 (/ screen-width 3) (/ screen-height 2)))
        
        ;; Score display - top left
        (.setColor @score-font Color/CYAN)
        (.draw @score-font @sprite-batch 
               (str "SCORE: " @score) 
               margin (- screen-height (* 2 line-height)))
        
        ;; High score
        (.setColor @score-font Color/GOLD)
        (.draw @score-font @sprite-batch 
               (str "HIGH: " @high-score) 
               margin (- screen-height (* 3 line-height)))
        
        ;; Stats - right side
        (.setColor @ui-font Color/WHITE)
        (.draw @ui-font @sprite-batch 
               (str "Time: " (format-time @game-time)) 
               (- screen-width 150) (- screen-height (* 2 line-height)))
        
        (.draw @ui-font @sprite-batch 
               (str "Caught: " @drops-caught) 
               (- screen-width 150) (- screen-height (* 3 line-height)))
        
        (.draw @ui-font @sprite-batch 
               (str "Missed: " @drops-missed) 
               (- screen-width 150) (- screen-height (* 4 line-height)))
        
        ;; Multiplier display
        (when (> @score-multiplier 1)
          (.setColor @score-font Color/ORANGE)
          (.draw @score-font @sprite-batch 
                 (str "MULTIPLIER x" @score-multiplier) 
                 (- screen-width 200) (- screen-height (* 5 line-height))))
        
        ;; Combo display - center of screen
        (when @show-combo
          (.setColor @title-font Color/RED)
          (.draw @title-font @sprite-batch 
                 (str "🔥 COMBO x" @combo-count " 🔥")
                 (/ screen-width 3) (/ screen-height 2)))
        
        ;; Navigation buttons with highlighting
        (draw-navigation-buttons screen-width screen-height margin)
        
        ;; Accuracy percentage - bottom left
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
  "Render the game - matches Java tutorial draw() method"
  (let [world-width (.getWorldWidth @viewport)
        world-height (.getWorldHeight @viewport)]

    ;; Clear screen
    (ScreenUtils/clear Color/BLACK)
    (.apply @viewport)
    (.setProjectionMatrix @sprite-batch (.combined (.getCamera @viewport)))

    ;; Begin drawing
    (.begin @sprite-batch)

    ;; Draw background first (draw order matters!)
    (.draw @sprite-batch @background-texture (float 0) (float 0) (float world-width) (float world-height))

    ;; Draw bucket
    (.draw @bucket-sprite @sprite-batch)

    ;; Draw all droplets
    (let [iter (.iterator @drop-sprites)]
      (while (.hasNext iter)
        (let [drop-sprite (.next iter)]
          (.draw drop-sprite @sprite-batch))))

    ;; End drawing
    (.end @sprite-batch)
    
    ;; Draw UI overlay
    (draw-ui)))

(defn create-main-app []
  "Create the main ApplicationListener - matches Java tutorial Main class"
  (proxy [ApplicationListener] []

    (create []
      (println "Initializing Drop Game...")

      ;; Load assets - exactly as in Java tutorial
      (reset! background-texture (Texture. "background.png"))
      (reset! bucket-texture (Texture. "bucket.png"))
      (reset! drop-texture (Texture. "drop.png"))

      ;; Load audio - with error handling for missing files
      (try
        (reset! drop-sound (.newSound Gdx/audio (.internal Gdx/files "drop.mp3")))
        (catch Exception _
          (println "Could not load drop.mp3 - continuing without sound")))

      (try
        (reset! music (.newMusic Gdx/audio (.internal Gdx/files "music.mp3")))
        (catch Exception _
          (println "Could not load music.mp3 - continuing without music")))

      ;; Initialize boilerplate rendering objects
      (reset! sprite-batch (SpriteBatch.))
      (reset! viewport (FitViewport. 8 5))
      (reset! ui-viewport (ScreenViewport.))
      (reset! glyph-layout (GlyphLayout.))
      
      ;; Create fonts for UI
      (create-fonts)

      ;; Initialize game objects
      (reset! bucket-sprite (Sprite. @bucket-texture))
      (.setSize @bucket-sprite 1 1)

      (reset! touch-pos (Vector2.))
      (reset! drop-sprites (Array.))
      (reset! bucket-rectangle (Rectangle.))
      (reset! drop-rectangle (Rectangle.))

      ;; Setup and play music
      (when @music
        (.setLooping @music true)
        (.setVolume @music 0.5)
        (.play @music))

      ;; Print game instructions
      (println "\n=== ENHANCED DROP GAME WITH NAVIGATION ===")
      (println "Movement Controls:")
      (println "  WASD or Arrow Keys - Move bucket left/right")
      (println "  Mouse - Click to move bucket")
      (println "Game Controls:")
      (println "  SPACE - Pause/Resume game")
      (println "  R - Restart game")
      (println "  ESC - Exit game")
      (println "")
      (println "🎯 Objective: Catch falling drops to score points!")
      (println "🔥 Build combos for bonus multipliers!")
      (println "⭐ Watch the navigation buttons highlight when pressed!")
      (println "==========================================\n")

      (println "Game initialized successfully!"))

    (resize [width height]
      ;; Update both viewports on window resize
      (.update @viewport width height true)
      (.update @ui-viewport width height true))

    (render []
      ;; Main game loop - organize into three methods as in tutorial
      (input)
      (logic)
      (draw))

    (pause [])
    (resume [])

    (dispose []
      ;; Clean up resources
      (println "Disposing game resources...")
      (when @background-texture (.dispose @background-texture))
      (when @bucket-texture (.dispose @bucket-texture))
      (when @drop-texture (.dispose @drop-texture))
      (when @drop-sound (.dispose @drop-sound))
      (when @music (.dispose @music))
      (when @sprite-batch (.dispose @sprite-batch)))))

(defn get-default-configuration []
  "Create LibGDX application configuration - matches LWJGL3Launcher"
  (doto (Lwjgl3ApplicationConfiguration.)
    (.setTitle "Enhanced Drop Game - Interactive Navigation | SPACE: Pause | R: Restart")
    (.setWindowedMode 800 500)  ; matches tutorial size
    (.setResizable false)))

(defn -main [& _args]
  "Main entry point"
  (println "Starting Drop Game...")
  (try
    (let [config (get-default-configuration)
          app-listener (create-main-app)]
      (Lwjgl3Application. app-listener config))
    (catch Exception e
      (println "Error starting game:" (.getMessage e))
      (.printStackTrace e)
      (System/exit 1))))

(comment
  ;; Development helpers
  (-main)
  )