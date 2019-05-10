(ns user.comm
  (:require [snow.comm.core :as comm]))

(print "helloaass")


(defn main! []
  (comm/start! (.getAttribute js/document.body "data-csrf-token")))

(main!)



