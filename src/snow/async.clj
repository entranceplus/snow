(ns snow.async
  (:require [clojure.core.async :refer [chan <!! >!! go <! >!] :as async]))

(def sz 4)

(defn subscribe
  "create a channel that subscribes to a mult channel subject to
   pub-topic-fn for pub and sub-topic-fn for sub"
  [mult-src-chan pub-topic-fn sub-topic-fn]
  (let [dest-chan (chan sz)
        dup-src-chan (chan sz)]
    (async/tap mult-src-chan dup-src-chan)
    (async/sub (async/pub dup-src-chan pub-topic-fn) sub-topic-fn dest-chan)
    dest-chan))

;; (defn subscribe-val
;;   "similar to subscribe but returns a atom which represents the
;;    most recent value returned by the subscribed channel"
;;   [src-chan pub-topic-fn sub-topic-fn]
;;   (let [*val* (atom {})
;;         sub-chan (subscribe src-chan pub-topic-fn sub-topic-fn)]
;;     (async/go-loop []
;;       (let [val (<! sub-chan)]
;;         (println "receiving new info .. resetting atom")
;;         (reset! *val* val)
;;         (recur)))
;;     *val*))
