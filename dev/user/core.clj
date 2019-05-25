(ns user.core
  (:require [snow.repl :as repl]
            [snow.server :as server]
            [shadow.cljs.devtools.server :as shadow-server]
            [shadow.cljs.devtools.api :as shadow]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]
            ))

;; (defn cljs-repl []
;;   (cemerick.piggieback/cljs-repl :app))

(defn restart! []
  (repl/stop!)
  (repl/start! server/system-config))

(restart!)

(defn -main [& args]
  (timbre/refer-timbre)
  (timbre/merge-config!
   {:appenders {:spit (appenders/spit-appender {:fname "snow.log"})}})
  (repl/start-nrepl)
  (repl/start! server/system-config)
  (shadow-server/start!)
  (shadow/dev :app))
