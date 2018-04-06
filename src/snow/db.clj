(ns snow.db
  (:require [clojure.java.jdbc :as jdbc]
            [conman.core :as conman]
            [honeysql.core :as sql]
            [environ.core :refer [env]]
            [honeysql.helpers :as helpers :refer :all :exclude [update]]
            [honeysql-postgres.format :refer :all]
            [honeysql-postgres.helpers :refer :all]
            [clojure.spec.alpha :as s]))

; (require '[clojure.spec.alpha :as s])
; (require '[expound.alpha :as expound])
; (set! s/*explain-out* expound/printer)

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn make-vec-if-not [maybe-vec]
  (if-not (sequential? maybe-vec)
    (conj []  maybe-vec)
    maybe-vec))

(defn query [db sqlmap]
  (jdbc/query db (-> sqlmap sql/build sql/format)))

(defn execute!
  ([db sqlmap]
   (jdbc/execute! db (let [sql (sql/format sqlmap)]
                        (println sql)
                        sql)))
  ([db]))

; (def schema {:idents {:users/by-id :users/id
;                       :users/all "users"}
;              :columns #{:users/username}})
;
; (def pathom-parse (p/parse {::p/plugins [(p/env-plugin
;                                            {::p/reader [sqb/pull-entities
;                                                         p/map-reader]})]}))
;
(defn remove-nil
  "remove the key from a map whose value is nil"
  [map]
  (->> map
       (filter (fn [[k v]] ((complement nil?) v)))
       (into {})))

(defn add
  "insert data into table, if a sequence of data is given then it is inserted in
   batch mode then number of inserts are returned. If a single map is passed then
   you get back the db representation with all columns"
  [db table data]
  (if (sequential? data)
    (execute! db (-> (insert-into table)
                     (values (mapv #'remove-nil data))))
    (->> data
         remove-nil
         (jdbc/insert! db table)
         first)))

;; (add voidwalker.content.core/db
;;               :posts
;;               {:url "fwe" :tags "fwe" :content "some" :title "fwefwef"})

;; (update voidwalker.content.core/db
;;         :posts
;;         {:set {:url "fwe" :title "fwefwef"}
;;          :where [:= :id 13]})


(defn update
  "update data in table"
  [db table update-map]
  (execute! db (-> (helpers/update table)
                   (sset (:set update-map))
                   (where (:where update-map)))))

(def db-spec
  {:classname   "org.postgresql.Driver"
   :dbtype "postgresql"
   :subprotocol "postgresql"})

(defn get-db-spec-from-env
  "creates a db spec from the environment variables,
   if the env variables are not found then tries the
   passed config"
  [& {:keys [config]}]
  (let [{:keys [dbuser db password host port]} config]
    (println "config passed is " config)
    (println "Trying environment variables" (env :dbuser))
    (merge db-spec {:user (or dbuser (env :dbuser))
                    :dbname (or db (env :dbname))
                    :password (or password (env :dbpassword))
                    :port (or port (env :dbport) 5432)
                    :host (or host
                              (env :dbhost)
                              "127.0.0.1")})))
