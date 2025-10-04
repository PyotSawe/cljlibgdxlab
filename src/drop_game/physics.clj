(ns drop-game.physics
  "Box2D Physics Engine integration for Enhanced Drop Game"
  (:import [com.badlogic.gdx.physics.box2d World BodyDef BodyDef$BodyType
            Body FixtureDef PolygonShape CircleShape Box2DDebugRenderer
            Contact ContactImpulse ContactListener Manifold]
           [com.badlogic.gdx.math Vector2]
           [com.badlogic.gdx Gdx]
           [com.badlogic.gdx.graphics.g2d SpriteBatch]
           [com.badlogic.gdx.graphics Camera]))

;; Physics world configuration
(def ^:const WORLD-WIDTH 8.0)
(def ^:const WORLD-HEIGHT 5.0)
(def ^:const PIXELS-PER-METER 100.0)
(def ^:const GRAVITY -9.8)
(def ^:const VELOCITY-ITERATIONS 8)
(def ^:const POSITION-ITERATIONS 3)

;; Physics categories for collision filtering
(def ^:const CATEGORY-BUCKET 0x0001)
(def ^:const CATEGORY-DROPLET 0x0002)
(def ^:const CATEGORY-WALL 0x0004)
(def ^:const CATEGORY-GROUND 0x0008)

;; Physics world and debug renderer
(def ^World physics-world (atom nil))
(def ^Box2DDebugRenderer debug-renderer (atom nil))
(def physics-enabled (atom true))

;; Body references
(def bucket-body (atom nil))
(def wall-bodies (atom []))
(def droplet-bodies (atom []))

;; Contact listener for collision detection
(def contact-listener
  (proxy [ContactListener] []
    (beginContact [contact]
      (let [fixture-a (.getFixtureA contact)
            fixture-b (.getFixtureB contact)
            body-a (.getBody fixture-a)
            body-b (.getBody fixture-b)
            user-data-a (.getUserData body-a)
            user-data-b (.getUserData body-b)]
        
        ;; Handle bucket-droplet collision
        (when (and user-data-a user-data-b)
          (cond
            (and (= user-data-a "bucket") (= user-data-b "droplet"))
            (handle-droplet-collision body-b)
            
            (and (= user-data-a "droplet") (= user-data-b "bucket"))
            (handle-droplet-collision body-a)
            
            (and (= user-data-a "droplet") (= user-data-b "ground"))
            (handle-droplet-ground-collision body-a)
            
            (and (= user-data-a "ground") (= user-data-b "droplet"))
            (handle-droplet-ground-collision body-b)))))
    
    (endContact [contact])
    (preSolve [contact old-manifold])
    (postSolve [contact impulse])))

;; Collision callbacks for game integration
(def collision-callback (atom nil))

(defn set-collision-callback [callback-fn]
  "Set the callback function for droplet collisions"
  (reset! collision-callback callback-fn))

(defn handle-droplet-collision [droplet-body]
  "Handle collision between bucket and droplet"
  (when @collision-callback
    (@collision-callback :caught droplet-body))
  ;; Mark droplet for removal
  (.setUserData droplet-body "remove"))

(defn handle-droplet-ground-collision [droplet-body]
  "Handle droplet hitting the ground"
  (when @collision-callback
    (@collision-callback :missed droplet-body))
  ;; Mark droplet for removal
  (.setUserData droplet-body "remove"))

(defn world-to-screen [world-coords]
  "Convert Box2D world coordinates to screen coordinates"
  (* world-coords PIXELS-PER-METER))

(defn screen-to-world [screen-coords]
  "Convert screen coordinates to Box2D world coordinates"
  (/ screen-coords PIXELS-PER-METER))

(defn create-physics-world []
  "Initialize the Box2D physics world"
  (let [world (World. (Vector2. 0 GRAVITY) true)]
    (.setContactListener world contact-listener)
    (reset! physics-world world)
    (reset! debug-renderer (Box2DDebugRenderer.))
    (println "Box2D Physics World created successfully!")
    world))

(defn create-bucket-body [x y width height]
  "Create a kinematic body for the bucket"
  (let [body-def (BodyDef.)
        _ (.set (.position body-def) (screen-to-world x) (screen-to-world y))
        _ (set! (.type body-def) BodyDef$BodyType/KinematicBody)
        
        body (.createBody @physics-world body-def)
        shape (PolygonShape.)
        _ (.setAsBox shape 
                     (screen-to-world (/ width 2)) 
                     (screen-to-world (/ height 2)))
        
        fixture-def (FixtureDef.)
        _ (set! (.shape fixture-def) shape)
        _ (set! (.density fixture-def) 1.0)
        _ (set! (.friction fixture-def) 0.5)
        _ (set! (.restitution fixture-def) 0.1)
        _ (set! (.filter.categoryBits fixture-def) CATEGORY-BUCKET)
        _ (set! (.filter.maskBits fixture-def) CATEGORY-DROPLET)]
    
    (.createFixture body fixture-def)
    (.setUserData body "bucket")
    (.dispose shape)
    (reset! bucket-body body)
    (println "Bucket physics body created")
    body))

(defn create-droplet-body [x y radius]
  "Create a dynamic body for a droplet"
  (let [body-def (BodyDef.)
        _ (.set (.position body-def) (screen-to-world x) (screen-to-world y))
        _ (set! (.type body-def) BodyDef$BodyType/DynamicBody)
        
        body (.createBody @physics-world body-def)
        shape (CircleShape.)
        _ (.setRadius shape (screen-to-world radius))
        
        fixture-def (FixtureDef.)
        _ (set! (.shape fixture-def) shape)
        _ (set! (.density fixture-def) 1.0)
        _ (set! (.friction fixture-def) 0.3)
        _ (set! (.restitution fixture-def) 0.4)
        _ (set! (.filter.categoryBits fixture-def) CATEGORY-DROPLET)
        _ (set! (.filter.maskBits fixture-def) (bit-or CATEGORY-BUCKET CATEGORY-WALL CATEGORY-GROUND))]
    
    (.createFixture body fixture-def)
    (.setUserData body "droplet")
    (.dispose shape)
    
    ;; Add some initial downward velocity for more realistic falling
    (.setLinearVelocity body 0 -2)
    
    (swap! droplet-bodies conj body)
    body))

(defn create-world-boundaries []
  "Create invisible walls and ground for the physics world"
  (let [world-width-m (screen-to-world WORLD-WIDTH)
        world-height-m (screen-to-world WORLD-HEIGHT)
        wall-thickness 0.1]
    
    ;; Ground
    (let [body-def (BodyDef.)
          _ (.set (.position body-def) (/ world-width-m 2) (- wall-thickness))
          _ (set! (.type body-def) BodyDef$BodyType/StaticBody)
          
          body (.createBody @physics-world body-def)
          shape (PolygonShape.)
          _ (.setAsBox shape (/ world-width-m 2) wall-thickness)
          
          fixture-def (FixtureDef.)
          _ (set! (.shape fixture-def) shape)
          _ (set! (.filter.categoryBits fixture-def) CATEGORY-GROUND)
          _ (set! (.filter.maskBits fixture-def) CATEGORY-DROPLET)]
      
      (.createFixture body fixture-def)
      (.setUserData body "ground")
      (.dispose shape)
      (swap! wall-bodies conj body))
    
    ;; Left wall
    (let [body-def (BodyDef.)
          _ (.set (.position body-def) (- wall-thickness) (/ world-height-m 2))
          _ (set! (.type body-def) BodyDef$BodyType/StaticBody)
          
          body (.createBody @physics-world body-def)
          shape (PolygonShape.)
          _ (.setAsBox shape wall-thickness (/ world-height-m 2))
          
          fixture-def (FixtureDef.)
          _ (set! (.shape fixture-def) shape)
          _ (set! (.filter.categoryBits fixture-def) CATEGORY-WALL)
          _ (set! (.filter.maskBits fixture-def) CATEGORY-DROPLET)]
      
      (.createFixture body fixture-def)
      (.setUserData body "wall")
      (.dispose shape)
      (swap! wall-bodies conj body))
    
    ;; Right wall
    (let [body-def (BodyDef.)
          _ (.set (.position body-def) (+ world-width-m wall-thickness) (/ world-height-m 2))
          _ (set! (.type body-def) BodyDef$BodyType/StaticBody)
          
          body (.createBody @physics-world body-def)
          shape (PolygonShape.)
          _ (.setAsBox shape wall-thickness (/ world-height-m 2))
          
          fixture-def (FixtureDef.)
          _ (set! (.shape fixture-def) shape)
          _ (set! (.filter.categoryBits fixture-def) CATEGORY-WALL)
          _ (set! (.filter.maskBits fixture-def) CATEGORY-DROPLET)]
      
      (.createFixture body fixture-def)
      (.setUserData body "wall")
      (.dispose shape)
      (swap! wall-bodies conj body))
    
    (println "World boundaries created")))

(defn update-bucket-position [x y]
  "Update bucket physics body position"
  (when @bucket-body
    (.setTransform @bucket-body 
                   (screen-to-world x) 
                   (screen-to-world y) 
                   0)))

(defn get-bucket-position []
  "Get bucket position from physics body"
  (when @bucket-body
    (let [pos (.getPosition @bucket-body)]
      [(world-to-screen (.x pos)) 
       (world-to-screen (.y pos))])))

(defn cleanup-marked-bodies []
  "Remove bodies marked for deletion"
  (let [bodies-to-remove (filter #(= (.getUserData %) "remove") @droplet-bodies)]
    (doseq [body bodies-to-remove]
      (.destroyBody @physics-world body)
      (swap! droplet-bodies #(remove #{body} %)))
    (count bodies-to-remove)))

(defn update-physics-world [delta-time]
  "Step the physics simulation forward"
  (when (and @physics-world @physics-enabled)
    ;; Step the physics world
    (.step @physics-world delta-time VELOCITY-ITERATIONS POSITION-ITERATIONS)
    
    ;; Clean up marked bodies
    (cleanup-marked-bodies)))

(defn render-physics-debug [camera]
  "Render Box2D debug visualization"
  (when (and @debug-renderer @physics-world)
    (.render @debug-renderer @physics-world (.combined camera))))

(defn get-droplet-positions []
  "Get positions of all droplet bodies for rendering"
  (map (fn [body]
         (let [pos (.getPosition body)]
           {:x (world-to-screen (.x pos))
            :y (world-to-screen (.y pos))
            :body body}))
       @droplet-bodies))

(defn dispose-physics-world []
  "Clean up physics world resources"
  (when @physics-world
    (.dispose @physics-world)
    (reset! physics-world nil))
  (when @debug-renderer
    (.dispose @debug-renderer)
    (reset! debug-renderer nil))
  (reset! bucket-body nil)
  (reset! wall-bodies [])
  (reset! droplet-bodies [])
  (println "Physics world disposed"))

;; Physics configuration functions
(defn toggle-physics []
  "Toggle physics simulation on/off"
  (swap! physics-enabled not)
  (println (str "Physics " (if @physics-enabled "enabled" "disabled"))))

(defn set-gravity [gravity-y]
  "Change world gravity"
  (when @physics-world
    (.setGravity @physics-world (Vector2. 0 gravity-y))
    (println (str "Gravity set to " gravity-y))))

(defn add-wind-force [force-x]
  "Apply wind force to all droplets"
  (doseq [body @droplet-bodies]
    (when (not= (.getUserData body) "remove")
      (.applyForceToCenter body (Vector2. force-x 0) true))))

(comment
  ;; Development helpers
  (create-physics-world)
  (create-bucket-body 400 50 64 64)
  (create-droplet-body 200 400 16)
  (create-world-boundaries)
  (toggle-physics)
  (set-gravity -15.0)
  (add-wind-force 2.0)
  )