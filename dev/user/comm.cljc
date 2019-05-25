(ns user.comm
  (:require [snow.comm.core :as comm]
            [re-frame.core :as rf]))

(print "helloaass")

(rf/reg-event-fx
 :db-update
 (fn [{:keys [db]} [_ d]]
   (println "reqyesting")
   {:db db
    ::comm/request {:data d
                    :on-success [::hello]
                    :on-failure [::error]}}))


(def sync (rf/->interceptor
           :id :echo
           :after (fn [{:keys [coeffects effects queue] :as m}]
                    (rf/dispatch [:db-update [::data 
                                              {::data    (:db effects)
                                               ::user-id (-> effects
                                                             :db
                                                             ::comm/user-id)}]])
                    m)))

(rf/reg-event-db
 :sample-update
 [sync]
 (fn [db [_]]
   (assoc db :a 12)))

(rf/dispatch [:db-update])

(defn main! []
  (comm/start! (.getAttribute js/document.body "data-csrf-token"))
  (rf/dispatch [:sample-update]))

(main!)



