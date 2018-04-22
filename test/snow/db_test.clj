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
  (delete-store db-path)
  (reset! conn (<!! (new-fs-store db-path)))
  (f)
  (delete-store db-path))



(use-fixtures :each db-fixture)

;; (def eid  "e6c994c7-db8c-41f2-8b1e-b88d6ed3991f")

(defn test-update [entity]
  (let [e (assoc entity :name "antash")]
    (update-entity  @conn ::test e)
    (= (get-entity @conn ::test (:id entity)) e)))


(deftest persistence-test
  (testing "crud entity"
    (let [eid   (-> @conn
                    (add-entity ::test entity)
                    :id)]
      ;; Add
      (is (some? eid))

      ;; Get by id
      (is (= (get-entity @conn ::test eid)
             (assoc entity :id eid)))

      ;; Get all
      (let [es (get-entity @conn ::test)]
        (is (= (count es) 1))
        (is (every? :id es)))

      ;; delete by id
      (delete-entity @conn ::test eid)

      ;; count after delete
      (is (= (count (get-entity @conn ::test))
             0)))))

