(ns snow.datomic
  (:require [datomic.api :as d]))

(defn transact [conn tx-data]
  @(d/transact conn (if (sequential? tx-data)
                      tx-data
                      [tx-data])))

(defn query [conn query]
  (println "Query is " query)
  (map first (d/q query (d/db conn))))
