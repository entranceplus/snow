(ns snow.db
  (:require [konserve.core :as k]
            [snow.util :as u]
            [clojure.spec.alpha :as s]
            [clojure.core.async :refer [<!!]]))

;(require '[clojure.spec.gen.alpha :as gen])


(defn add-entity
  [conn spec {:keys [id] :as item}]
  (let [eid (or id  (u/uuid))]
    {:status (<!! (k/assoc-in conn [spec (keyword  eid)] (s/conform spec item)))
     :id eid}))

(defn delete-entity
  [conn spec id]
  (<!! (k/update-in conn [spec] (fn [coll] (dissoc coll (keyword id))))))

(defn get-entity
  ([conn spec] (map (fn [[id e]]
                      (merge {:id (name id)}
                             e)) (<!! (k/get-in conn [spec]))))
  ([conn spec id] (-> (<!! (k/get-in conn [spec (keyword  id)]))
                      (assoc :id id))))

(defn update-entity
  [conn spec id entity]
  (<!! (k/update-in conn [spec] (fn [coll] (assoc (keyword id) entity)))))
