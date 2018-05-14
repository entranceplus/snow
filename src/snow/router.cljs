(ns snow.router
  (:require [bide.core :as b]
            [reagent.core :as r]
            [re-frame.core :as rf]))

(defn transform-map [rmap]
  (into []
        (for [[r u] rmap]
          [u r])))

(defn make-bide-router [rmap]
  (b/router (transform-map rmap)))


(defn start! [route-map on-navigate]
  (let [router (make-bide-router route-map)]
    (rf/reg-fx ::navigate (fn [[route params]]
                            (println "reying to navigate to " route params)
                            (when (some? route)
                              (b/navigate! router route params))))
    (b/start! router {:default :voidwalker.home
                      :html5? true
                      :on-navigate on-navigate})))
