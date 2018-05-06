(ns snow.db
  (:require [konserve.core :as k]
            [snow.comm.core :as comm]
            [re-frame.core :as rf]
            [snow.util :as u]
            [clojure.spec.alpha :as s]
            [clojure.core.async :refer [<!!]]))

;(require '[clojure.spec.gen.alpha :as gen])


(defn add-entity
  [conn spec {:keys [id] :as item}]
  (let [eid (or id  (u/uuid))]
    {:status (<!! (k/assoc-in conn [spec (keyword  eid)] (s/assert spec item)))
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


;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Some helpers for frp ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-handler [{{:keys [::entity ::entity-key]} :data {{conn :store} :conn} :component}]
  (add-entity conn entity-key entity)
  (rf/dispatch [::updated entity-key]))

;; (rf/reg-event-fx ::add
;;                  (fn [{:keys [db]} [_ espec data]]
;;                    {:db db
;;                     ::request {:snow.comm/type :snow.db/add
;;                                :snow.db/entity-key espec
;;                                :snow.db/entity data}}))

;; (rf/reg-event-fx )

;; (defmethod comm/request-handler 
;;   ::add
;;   [{{:keys [::entity ::entity-key]} :data {{conn :store} :conn} :component}]
;;   (println "entity is " entity " store " conn)
;;   (add-entity conn entity-key entity)
;;   (rf/dispatch [::updated entity-key]))    

;; (defmethod comm/reques-handler ::get []
;;   (println "ab dega mai data"))
