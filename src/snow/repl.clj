(ns snow.repl
  (:require [snow.systems :as sys]
            [snow.env :refer [read-edn]]
            [defn-spec.core :as ds]
            [clojure.spec.alpha :as s]
            [clojure.tools.deps.alpha.repl :refer [add-lib]]
            [clojure.tools.nrepl.server :as nrepl]
            [cider.nrepl :refer [cider-nrepl-handler]]
            [snow.util :as u]))

(def config (read-edn "profiles.edn"))

(def state (atom {}))

(defn system []
  (-> @state ::sys/repl first))

(ds/defn-spec start-systems
  {::s/args (s/cat :config ::sys/systems)}
  [config-map]
  (println "Config map is " config-map)
  (swap! state assoc ::sys/repl (sys/start-systems config-map)))

(defn stop! []
  (sys/stop-systems (get ::sys/repl @state)))

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
                      :handler cider-nrepl-handler))
