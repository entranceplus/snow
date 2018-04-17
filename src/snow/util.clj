(ns snow.util)

(defn make-int [n]
  (cond-> n
    (string? n) Integer.))

(defn map-hash
  "map over hash map and get map back"
  [f m]
  (into {} (map f m)))

(defn make-vec-if-not
  [maybe-vec]
  "deprecated please refer to snow.util/make-vec-if-not"
  (if-not (sequential? maybe-vec)
    (conj []  maybe-vec)
    maybe-vec))
