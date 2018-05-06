(ns snow.comm.core
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [taoensso.sente :as sente]
            [compojure.core :refer [routes GET POST]]
            [com.stuartsierra.component :as component]
            [system.components.sente :refer [new-channel-socket-server]]
            [taoensso.sente.server-adapters.immutant      :refer (get-sch-adapter)]))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; reframe on clojure  ;;
;;;;;;;;;;;;;;;;;;;;;;;;;


;; {::type :snow.db/add
;;  :snow.db/entity-key :voidwalker.content/post
;;  :snow.db/entity :article-data
;;  :dispatch [::articles-updated]}

(rf/reg-event-fx
 ::trigger
 [(rf/inject-cofx :system)]
 (fn [{:keys [db system]} [_ data]]
   {::request {:data data
               :component system}
    :db db}))

(rf/reg-event-db
 :chsk/ws-ping
 (fn [db _]
  (println "ping received")
  db))

(rf/reg-event-fx
 ::broadcast
 (fn [{db :db} [_ data]]
  {::broadcast data
   :db db}))

(defn broadcast [chsk-send! connected-uids data]
  (println "connected uids are " connected-uids)
  (doseq [uid (:any @connected-uids)]
    (println "Sending message to client")
    (chsk-send! uid [::data data])))

(defn event-msg-handler [component]
  (fn [ev-msg]
    (println "recevied a message ev-msg " (:event ev-msg))
    (rf/dispatch (:event ev-msg))))

(defn sente-routes [{{{:keys [ring-ajax-get-or-ws-handshake ring-ajax-post]} :comm} :comm}]
  (routes
    (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
    (POST "/chsk" req (ring-ajax-post                req))))

(defn start-sente [event-msg-handler]
  (component/start (new-channel-socket-server event-msg-handler (get-sch-adapter) {:wrap-component? true})))

(defrecord Comm [event-msg-handler broadcast request-handler init-data]
  component/Lifecycle
  (start [component]
    (let [{:keys [chsk-send! connected-uids] :as sente} (start-sente event-msg-handler)]
      (rf/reg-fx ::broadcast (partial broadcast chsk-send! connected-uids))
      (rf/reg-fx ::request request-handler)
      (rf/reg-cofx :system (fn [coeffects]
                             (assoc coeffects :system component))) 
      (assoc component :comm sente)))
  (stop [component]
    (if-let [sente (:sente component)]
      (do (component/stop sente)
          (dissoc component :comm))
      component)))

(defn new-comm [event-msg-handler broadcast request-handler]
  (map->Comm {:event-msg-handler event-msg-handler
              :request-handler request-handler
              :broadcast broadcast}))
