(ns snow.ui.components
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

(defn get-value [e]
  (-> e .-target .-value))

(defn input [{:keys [state placeholder type class]}]
  [:div.field>div.control [:input.input
                           {:placeholder placeholder
                            :class class
                            :value @state
                            :type (or type "text")
                            :on-change #(reset! state (-> % get-value))}]])


(rf/reg-sub
 ::input
 (fn [db [_ k]]
   (if (vector? k)
     (get-in db k)
     (get db k))))

;; (defn item-sorter
;;   [items _]
;;   (sort items))

;; (reg-sub 
;;  :sorted-items 
;;  (fn [_ _]  (subscribe [:items]))
;;  item-sorter)

(rf/reg-sub
 ::processed-input
 (fn [_ [_ k _]] (rf/subscribe [::input k]))
 (fn [input [_ k f]]
   (f input)))

(rf/reg-event-db
 ::set-input
 (fn [db [_ k v]]
   (if (vector? k)
     (assoc-in db k v)
     (assoc db k v))))

(defn rx-input [{:keys [db-key placeholder type class read-fn write-fn]
                 :or {read-fn identity
                      write-fn identity}}]
  (println "hello?")
  [:div.field>div.control [:input.input
                           {:placeholder placeholder
                            :class class
                            :value @(rf/subscribe [::processed-input db-key read-fn])
                            :type (or type "text")
                            :on-change #(rf/dispatch [::set-input
                                                      db-key
                                                      (-> % get-value write-fn)])}]])
