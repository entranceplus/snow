(ns snow.boot
  (:require [ragtime.jdbc :as jdbc]
            [ragtime.repl :as repl]
            [snow.db :as db]
            [clojure.edn :as edn]
            [environ.boot :refer [environ]]
            [boot.core :refer [deftask with-pre-wrap]]))

(def read-edn (comp edn/read-string slurp))

(def profile (fn []
               (read-edn "profiles.edn")))

(defn config []
  {:datastore  (jdbc/sql-database (db/get-db-spec-from-env :config (profile)))
   :migrations (jdbc/load-resources "migrations")})

(deftask migrate
  "Task to run a db migration"
  []
  (with-pre-wrap [fs]
    (repl/migrate (config))
    fs))

(deftask rollback
  "Task to run a db rollback"
  []
  (with-pre-wrap [fs]
    (repl/rollback (config))
    fs))

(deftask local-migrate []
  (comp  (environ :env (profile))
         (migrate)))

(deftask local-rollback []
  (comp (environ :env (profile))
        (rollback)))
