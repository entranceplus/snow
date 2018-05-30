(ns snow.s3
  (:require [snow.env :refer [profile]]
            [amazonica.aws.s3 :as s3]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [amazonica.aws.s3transfer :as s3transfer])
  (:import [com.amazonaws.services.s3.model CannedAccessControlList]))

(def cred (let [{:keys [reigon aws-access-key-id aws-secret-access-key]} (profile)]
            {:endpoint reigon
             :access-key aws-access-key-id
             :secret-key aws-secret-access-key}))

(defn s3-key [key-or-str]
  (cond-> key-or-str
    (keyword? key-or-str) name))

(defn get-bucket-name
  "will return the bucket name of the first bucket it finds in the account"
  []
  (:name (first (s3/list-buckets cred))))

(defn read-base64 [d]
  (-> d
     (subs (+ (str/index-of d ",") 1))
     .getBytes
     org.apache.commons.codec.binary.Base64/decodeBase64))

(defn string-request [{:keys [content binary]}]
  (let [bytes (if (true?  binary)
                (read-base64 content)
                (.getBytes content "UTF-8"))]
    {:input-stream (java.io.ByteArrayInputStream. bytes)
     :metadata {:content-length (count bytes)}}))

(defn upload
  [key {:keys [file content binary] :as m}]
  (let [bucket-name (get-bucket-name)
        key-str (s3-key key)]
    (println "Uploading file " key)
    (s3/put-object cred
                   (merge {:bucket-name bucket-name
                           :canned-acl CannedAccessControlList/PublicRead
                           :key key-str}
                          (if (some? content)
                            (string-request m)
                            {:file file})))
    (println "Upload complete. Retreiving url")
    (s3/get-resource-url cred bucket-name key)))

(defn get-file [key]
  (s3/get-object cred
                 :bucket-name (get-bucket-name)
                 :key (s3-key key)))

(defn read-file [key]
  (-> key get-file :object-content slurp))

#_(def test "into-space-2-3840x2160_44765-mm-90.jpg")
#_(upload "junkmap" {:content "akash shakdwipeea"})

#_(s3/get-resource-url cred (get-bucket-name) test)
#_(upload :file-junk {:file "./build.boot"})

#_(get-file test)

;; set S3 Client Options
#_(s3/list-buckets cred
                   {:client-config {}
                    :path-style-access-enabled false
                    :chunked-encoding-disabled false
                    :accelerate-mode-enabled false
                    :payload-signing-enabled true
                    :dualstack-enabled true
                    :force-global-bucket-access-enabled true})
