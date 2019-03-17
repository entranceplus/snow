;; (ns snow.boot
;;   (:gen-class)
;;   (:require [ragtime.jdbc :as jdbc]
;;             [ragtime.repl :as repl]
;;             [snow.mysql :as mysql]
;;             [clojure.edn :as edn]
;;             [environ.boot :refer [environ]]
;;             [boot.core :refer [deftask with-pre-wrap]]))

;; (defn config []
;;   {:datastore  (jdbc/sql-database (mysql/get-db-spec-from-env))
;;    :migrations (jdbc/load-resources "migrations")})

;; (deftask migrate
;;   "Task to run a db migration"
;;   []
;;   (with-pre-wrap [fs]
;;     (repl/migrate (config))
;;     fs))

;; (deftask rollback
;;   "Task to run a db rollback"
;;   []
;;   (with-pre-wrap [fs]
;;     (repl/rollback (config))
;;     fs))

;; (deftask gen-migrate
;;   "create a new migration"
;;   [name]
;;   (with-pre-wrap [fs]
;;     (let [curr (.format (java.text.SimpleDateFormat. "yyyyMMddHHmmss")
;;                         (java.util.Date.))
;;           name (str "migrations/" curr name)]
;;       (spit (str name ".up.sql") "-- Migrations go here")
;;       (spit (str name ".down.sql") "-- Rollbacks go here")
;;       (println "Migrations " name "generated"))))

;; (deftask local-migrate []
;;   (comp  (environ :env (profile))
;;          (migrate)))

;; (deftask local-rollback []
;;   (comp (environ :env (profile))
;;         (rollback)))
