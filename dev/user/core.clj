(ns user.core
  (:require [cider.nrepl :refer [cider-nrepl-handler]]))

(defn -main [& args]
  (nrepl.server/start-server :port 9001 :handler cider-nrepl-handler)
  (println "nrepl started"))
