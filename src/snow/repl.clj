(ns snow.repl
  (:require [snow.systems :as sys]
            [snow.util :as u]))

(def state (atom {}))

(defn system []
  (-> @state ::sys/repl first))

(defn start-systems [config]
  (swap! state assoc ::sys/repl (sys/start-systems (u/make-vec-if-not config))))

(defn stop-systems []
  (sys/stop-systems (get ::sys/repl @state)))

