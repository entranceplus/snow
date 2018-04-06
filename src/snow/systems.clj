(ns snow.systems
  (:gen-class)
  (:require [clojure.spec.alpha :as s]
            [environ.core :refer [env]]
            [com.stuartsierra.component :as component]
            [system.components.repl-server :refer [new-repl-server]]
            [safely.core :refer [safely]]))

(s/def ::system #(instance? com.stuartsierra.component.SystemMap %))

(s/def ::config map?)

(s/def ::system-fn any?)

(s/def ::system-def (s/keys :req [::system-fn ::config]))

(s/def ::systems (s/coll-of ::system-def :kind vector?))

(s/def ::system-vars (s/coll-of ::system))

(s/def ::systems-vec (s/coll-of ::system-vars))

(defn sysmap-gen [system-config & {:keys [config]}]
  (let [config-map (if (some? config) config env)]
    (system-config config-map)))

(defn make-system-map [system-fn config prod?]
  (apply component/system-map (cond-> (system-fn config)
                                prod? (conj :repl-server (-> :repl-port
                                                             config
                                                             read-string
                                                             new-repl-server)))))


(defn gen-system
  ([system-config-gen-fn]
   (gen-system system-config-gen-fn env))
  ([system-config-gen-fn config & {:keys [prod?]}]
   (make-system-map system-config-gen-fn config prod?)))

(s/fdef start-systems
        :args (s/cat :systems ::systems)
        :ret ::systems-vec)

(defn start-systems [systems & {:keys [prod?]}]
  (doall (map (fn [{:keys [snow.systems/system-fn snow.systems/config]}]
                (println "Starting a system with config " config " system-fn is " system-fn)
                (if prod?
                  (safely (component/start (gen-system system-fn config :prod? prod?))
                          :on-error
                          :max-retry 20
                          :retry-delay [:random-exp-backoff :base 3000 :+/- 0.50]
                          :message (str "System start failed for config " config))
                  (component/start (gen-system system-fn config :prod? prod?))))
              systems)))

(s/fdef stop-system
        :args (s/cat :systems ::systems-vec)
        :ret ::systems-vec)

(defn stop-systems [systems]
  (map #(component/stop %) systems))

(defn get-port [config]
  (let [port (config :http-port)]
    (cond-> port
      (string? port) Integer.)))
