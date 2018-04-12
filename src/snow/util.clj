(ns snow.util)

(defn make-int [n]
  (cond-> n
    (string? n) Integer.))

(defn map-hash
  "map over hash map and get map back"
  [f m]
  (into {} (map f m)))
