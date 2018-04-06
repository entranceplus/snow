(ns snow.util)

(defn map-hash
  "map over hash map and get map back"
  [f m]
  (into {} (map f m)))    
