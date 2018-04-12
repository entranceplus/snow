(ns snow.client
  (:require [clj-http.client :as client]
            [muuntaja.core :as m]))

(def m (m/create))

(defn read-json [json]
  (->> json
       (m/decode m "application/json")))

(defn write-json [json]
  (->> json
       (m/encode m "application/json")
       slurp))

(defn get
  "simple json request"
  ([url] (get url nil))
  ([url opts]
   (-> url
       (client/get (merge {:as :json
                           ; :coerce :always
                           :throw-exceptions false} opts))
       :body)))

(defn post
  "simple json post"
  [url & {:keys [body headers]}]
  (client/post url
               {:content-type :json
                :accept :json
                :as :json
                :throw-exceptions false
                :body (cond-> body
                        ((complement string?) body) write-json)
                :headers headers}))

(defn delete [url & {:keys [headers]}]
  (client/delete url
                 {:accept :json
                  :as :json
                  :coerce :always
                  :throw-exceptions false
                  :headers headers}))

(defn restclient
  ([base-url]
   (restclient base-url {}))
  ([base-url {:keys [api-key]}]
   (fn [request-type url & {:keys [body]}]
     (let [headers (when api-key {:authorization (str "Bearer " api-key)})]
       (case request-type
         :get (get (str base-url url)
                   {:headers headers})
         :post (post (str base-url url)
                     :body body
                     :headers headers)
         :delete (delete (str base-url url)
                         :headers headers))))))
