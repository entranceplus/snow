(ns snow.comm.core
  (:require [re-frame.core :as rf]
            [clojure.spec.alpha :as s]
            [taoensso.sente :as sente]
            [taoensso.timbre :as timbre :refer [info]]
            [compojure.core :refer [routes GET POST]]
            [com.stuartsierra.component :as component]
            [system.components.sente :refer [new-channel-socket-server]]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [go]]
            [taoensso.sente.server-adapters.immutant :refer (get-sch-adapter)]))

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
 (fn [{:keys [db system]} [_ data ?reply-fn]]
   (info "fn is trigger" (fn? ?reply-fn))
   {::request {:data data
               :component system
               :?reply-fn ?reply-fn}
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
    (info "Sending message to client")
    (chsk-send! uid [::data data])))

(defn send-client-message [chsk-send! {:keys [user-id data] :as n}]
  (info "nat " n)
  (chsk-send! (symbol user-id) [::data data]))

(defn event-msg-handler [component]
  (fn [{:keys [event ?reply-fn] :as ev-msg}]
    (info "recevied a message ev-msg1 " (first event)) 
    (rf/dispatch (conj event ?reply-fn))))

(defn sente-routes [{{{:keys [ring-ajax-get-or-ws-handshake ring-ajax-post]} :comm} ::comm}]
  (routes
   (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
   (POST "/chsk" req (ring-ajax-post                req))))

(defn start-sente [event-msg-handler]
  (component/start (new-channel-socket-server event-msg-handler
                                              (get-sch-adapter)
                                              {:wrap-component? true
                                               :user-id-fn (fn [ring-req]
                                                             (gensym))})))

(defrecord Comm [event-msg-handler broadcast request-handler init-data]
  component/Lifecycle
  (start [component]
    (let [{:keys [chsk-send! connected-uids] :as sente} (start-sente event-msg-handler)]
      (rf/reg-fx ::broadcast (partial broadcast chsk-send! connected-uids))
      (rf/reg-fx ::message (partial send-client-message chsk-send!))
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
