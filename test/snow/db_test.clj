(ns snow.db-test
  (:require [snow.db :refer :all]
            [konserve.core :as k]
            [konserve.filestore :refer [new-fs-store delete-store]]
            [clojure.core.async :refer [<!!]]
            [clojure.spec.alpha :as s]
            [clojure.java.io :as io]
            [clojure.test :refer :all]))

(s/def ::test (s/keys :req-un [::name ::data]))

(def db-path "/tmp/store")

(def conn (atom {}))

(def entity {:name "akash"
             :data [{:value "nested"}]})

(defn db-fixture [f]
  (-> db-path
      io/as-file
      io/make-parents)
  (reset! conn (<!! (new-fs-store db-path)))
  (f)
  (delete-store db-path))



(use-fixtures :each db-fixture)

;; (def eid "c69953d3-309a-469c-a2d6-55e599e4bd15")

;; (require '[snow.util :as u])

;; (def id (snow.util/uuid))

;; (<!! (k/assoc-in @conn [::test id] entity))

;; (<!! (k/get-in @conn [::test id]))
(deftest persistence-test
  (testing "crud entity"
    (let [store @conn
          eid   (-> @conn
                    (add-entity ::test entity)
                    :id)]
      (is (some? eid))
      (is (= (get-entity @conn ::test eid)
             entity))
      (is (= (count (get-entity store ::test))
             1))
      (delete-entity store ::test eid)
      (is (= (count (get-entity store ::test))
             0)))))

