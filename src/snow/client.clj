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

(def default-opts {:content-type :json
                   :accept :json
                   :as :json
                   :throw-exceptions false})

(defn get
  "simple json request"
  ([url] (get url nil))
  ([url opts]
   (-> url
      (client/get (merge default-opts opts))
      :body)))

(defn prepare-json-body [{body :body :as opts}]
  (merge default-opts
         (assoc opts :body (cond-> body
                             ((complement string?) body) write-json))))

(defn post
  "simple json post"
  ([url] (post url nil))
  ([url {:keys [body headers] :as opts}]
   (->> opts
      prepare-json-body
      (client/post url))))

(defn put
  ([url] (put url nil))
  ([url {body :body :as opts}]
   (->> opts
      prepare-json-body
      (client/put url))))

(defn patch
  ([url] (patch nil))
  ([url opts]
   (->> opts
      prepare-json-body
      (client/patch url))))

(defn delete
  ([url] (delete url nil))
  ([url opts]
   (client/delete url
                  (merge {:accept :json
                          :as :json
                          :coerce :always
                          :throw-exceptions false}
                         opts))))

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
