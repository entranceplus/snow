(ns snow.repl
  (:require [snow.systems :as sys]
            [snow.env :refer [read-edn]]
            [taoensso.timbre.appenders.core :as appenders]
            [defn-spec.core :as ds]
            [clojure.spec.alpha :as s]
            ;; [clojure.tools.deps.alpha.repl :refer [add-lib]]
            [clojure.tools.nrepl.server :as nrepl]
            [cider.nrepl :refer [cider-nrepl-handler]]
            [taoensso.timbre :as timbre]
            [snow.util :as u]))

(def config (try (read-edn "profiles.edn")
                 (catch Exception e)))

(def state (atom {}))

#_(defn cljs-repl []
    (cemerick.piggieback/cljs-repl :app))

#_(def add-dep add-lib)

(timbre/merge-config!
 {:appenders {:spit (appenders/spit-appender {:fname (:log config)})}})

(defn system []
  (-> @state ::sys/repl))

(ds/defn-spec start-systems
  {::s/args (s/cat :config ::sys/systems)}
  [config-map]
  (println "Config map is " config-map)
  (swap! state assoc ::sys/repl (sys/start-systems config-map)))

(defn stop! []
  (sys/stop-systems (get @state ::sys/repl)))

(defn sys-map [f config]
  {::sys/system-fn f
   ::sys/config config})

(defn start!
  ([f]
   (start! f (read-edn "profiles.edn")))
  ([fn config]
   (start-systems (if (coll? fn)
                    (map #(-> % (sys-map config)) fn)
                    (-> fn
                       (sys-map config)
                       u/make-vec-if-not)))))


(defn start-nrepl []
  (nrepl/start-server :port (or (:repl-port config)
                               9001)
                      :host "localhost"
                      :handler cider-nrepl-handler))
