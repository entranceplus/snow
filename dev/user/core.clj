(ns user.core
  (:require [snow.repl :as repl]
            [snow.server :as server]
            [shadow.cljs.devtools.server :as shadow-server]
            [shadow.cljs.devtools.api :as shadow]
            ))

;; (defn cljs-repl []
;;   (cemerick.piggieback/cljs-repl :app))

(defn -main [& args]
  (repl/start-nrepl)
  (repl/start! server/system-config)
  (shadow-server/start!)
  (shadow/dev :app))
