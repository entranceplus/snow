(ns snow.server
  (:require [com.stuartsierra.component :as component]
            [snow.comm.core :as comm]
            [snow.systems :as system]
            [snow.client :as client]
            [snow.env :as env]
            [re-frame.core :as rf]
            [compojure.core :refer [routes GET ANY]]
            [ring.util.http-response :as response]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :as params]
            [ring.middleware.anti-forgery :as anti-forgery]
            [hiccup.core :as h]
            [taoensso.timbre :as timbre :refer [info]]
            (system.components
             [immutant-web :refer [new-immutant-web]]
             [endpoint :refer [new-endpoint]]
             [middleware :refer [new-middleware]]
             [handler :refer [new-handler]])))


;;;;;;;;;;;;;;;
;; home-page ;;
;;;;;;;;;;;;;;;

(defn home-page [csrf-token]
  [:html
   [:head
    [:meta {:content "text/html; charset=UTF-8"
            :http-equiv "Content-Type"}]
    [:meta {:content "width=device-width, initial-scale=1"
            :name "viewport"}]
    [:title "snow"]]
   [:body {:data-csrf-token csrf-token}
    [:div {:id "app"}]
    [:script {:src "/js/main.js"
              :type "text/javascript"}]]])


(defn serve-page [req]
  (-> req
     :anti-forgery-token
     home-page
     h/html
     response/ok
     (response/header "Content-Type" "text/html"))) 


(defn site [_]
  (routes
   (GET "/" req (serve-page req))
   (ANY "*" req (serve-page req))))

;; (keys m1)

(rf/reg-event-db
 :sync-data
 (fn [db [_ [_ {:keys [:user.comm/data :user.comm/user-id] :as m} ]]]
   (info "data" m)
   (def m1 m)
   (assoc-in db
             [:sync-data (keyword  user-id)]
             data)))


(rf/reg-event-fx
 :client-sync
 (fn [{db :db} [_ user-id]]
   (info "user id " user-id)
   {:db db
    ::comm/message {:user-id user-id
                    :data [::comm/trigger [::update-db
                                           (get-in db
                                                   [:sync-data
                                                    (keyword user-id)])]]}}))


(rf/reg-sub
 :sync-data
 (fn [db [_]] (get db :sync-data)))

(def a @(rf/subscribe [:sync-data]))

(keys a)

(defn request-handler [{:keys [event ?reply-fn data] :as ev-msg}]
  (println "asdhasdhioa")
  (info "ever" event)
  (def d ev-msg)
  (rf/dispatch [:sync-data event])
  ;; (when (some? ?reply-fn))
  (?reply-fn "synced"))

;; ((:?reply-fn d) [:snow.comm.core/trigger (search-yelp (:data d))])

;; (rf/dispatch [:client-sync "G__62751"])
;; (rf/dispatch [::comm/broadcast {:dispatch [:sample-update]}])


(defn system-config [config]
  [::comm/comm (comm/new-comm (fn [component] request-handler)
                              comm/broadcast
                              request-handler)
   :middleware (new-middleware {:middleware [wrap-session
                                             anti-forgery/wrap-anti-forgery
                                             params/wrap-params
                                             wrap-keyword-params
                                             [wrap-resource "public"]]})
   ::site-endpoint (component/using (new-endpoint site)
                                    [:middleware])
   ::sente-endpoint (component/using (new-endpoint comm/sente-routes)
                                     [:middleware ::comm/comm])
   ::handler (component/using (new-handler)
                              [::sente-endpoint
                               ::site-endpoint
                               :middleware])
   ::api-server (component/using
                 (new-immutant-web :port
                                   (system/get-port config :http-port))
                 [::handler])])
