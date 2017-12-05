(ns snow.client
  (:require [clj-http.client :as client]))

(defn get
  "simple json request"
  ([url] (get nil))
  ([url opts]
   (-> url
       (client/get (merge {:as :json} opts))
       :body)))
