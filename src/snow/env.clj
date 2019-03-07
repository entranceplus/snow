(ns snow.env
  (:require [clojure.edn :as edn]))

(def read-edn (comp edn/read-string slurp))

(def profile (fn []
               (try (read-edn "profiles.edn")
                    (catch Exception ex {}))))
