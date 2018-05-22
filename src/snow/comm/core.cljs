(ns snow.comm.core
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require  [cljs.core.async :as async :refer (<! >! put! chan)]
             [taoensso.encore :as encore :refer-macros (have have?)]
             [taoensso.sente  :as sente :refer (cb-success?)]
             [re-frame.core :as rf]))


;; websocket setup
(defn start-socket []
  (println "connecting to wsa")
  (let [{:keys [chsk ch-recv send-fn state] :as socket}
        (sente/make-channel-socket! "/chsk" ; Note the same path as before
                                    {:type :auto
                                     :protocol :http
                                     :host "localhost:8000"})] ; e/o #{:auto :ajax :ws}
    (def chsk       chsk)
    (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
    (def chsk-send! send-fn) ; ChannelSocket's send API fn
    (def chsk-state state)
    socket))   ; Watchable, read-only atom


;; re-frame stuff

;; (rf/reg-event-fx
;;  ::trigger
;;  (fn [{:keys [db]} [_ data]]
;;    {::request [::trigger data]
;;     :db db}))

(defn query-handler [chsk-send! {:keys [data on-success on-failure] :as q}]
  (println "go " (fn? chsk-send!))
  (chsk-send! data
              8000
              (fn [reply]
                (if (sente/cb-success? reply)
                  (do (println "success sending msg")
                      (when (vector? on-success) (rf/dispatch on-success)))
                  (do (println "error sending msg " reply)
                      (when (vector? on-failure) (rf/dispatch on-failure)))))))

;; (rf/dispatch [::trigger {:snow.comm.core/type :snow.db/add
;;                          :data 90}])

;;;; Sente event handlers

(def ->output! println)

;; (defmulti -event-msg-handler
;;   "Multimethod to handle Sente `event-msg`s"
;;   :id) ; Dispatch on event-id


;; (defn event-msg-handler
;;   "Wraps `-event-msg-handler` with logging, error catching, etc."
;;   [{:as ev-msg :keys [id ?data event]}]
;;   (-event-msg-handler ev-msg))

;; (defmethod -event-msg-handler
;;   :default ; Default/fallback case (no other matching handler)
;;   [{:as ev-msg :keys [event]}]
;;   (->output! "Unhandled event: %s" event))
(rf/reg-event-fx
 :chsk/recv
 (fn [{db :db} [_ {:as ev-msg :keys [?data]}]]
   (->output! "Push event from server:")
   (let [data (-> ?data second :dispatch)]
     (cond-> {:db db}
       (boolean data) (merge {:dispatch data})))))

(rf/reg-event-fx
 :chsk/handshake
 (fn  [{:keys [db] :as e} [_ {:as ev-msg :keys [?data]}]]
   (let [[?uid ?csrf-token ?handshake-data] ?data]
     (->output! "Handshake: %s " ?data)
     {:db db
      :dispatch [::connected]})))

(rf/reg-event-fx
 :chsk/state
 (fn [{db :db} [_ {:as ev-msg :keys [?data]}]]
   (let [[old-state-map new-state-map] (have vector? ?data)]
     (println "socket state change " new-state-map)
     {:db (assoc db ::connected (:open? new-state-map))})))

(def data "ok")

(defn event-msg-handler [{:as ev-msg :keys [id ?data event]}]
  (rf/dispatch [id ev-msg]))

(defonce router_ (atom nil))
(defn  stop-router! [] (when-let [stop-f @router_] (stop-f)))
(defn start-router! []
  (println "Starting router ")
  (stop-router!)
  (let [socket (start-socket)]
    (reset! router_
            (sente/start-client-chsk-router!
             (:ch-recv socket) event-msg-handler))
    (rf/reg-fx ::request (partial query-handler (:send-fn socket)))))



(defn start! [] (start-router!))

;; (defonce _start-once (start!))
