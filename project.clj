(defproject libgdx-drop-game "0.1.0-SNAPSHOT"
  :description "A simple drop game using libGDX and Clojure"
  :url "https://github.com/your-username/libgdx-drop-game"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [com.badlogicgames.gdx/gdx "1.12.1"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl3 "1.12.1"]
                 [com.badlogicgames.gdx/gdx-platform "1.12.1" :classifier "natives-desktop"]
                 [com.badlogicgames.gdx/gdx-box2d "1.12.1"]
                 [com.badlogicgames.gdx/gdx-box2d-platform "1.12.1" :classifier "natives-desktop"]]
  :source-paths ["src"]
  :resource-paths ["resources"]
  :main ^:skip-aot drop-game.main-physics
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}
  :jvm-opts ["-Djava.awt.headless=false"])