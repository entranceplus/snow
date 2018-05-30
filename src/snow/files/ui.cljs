(ns snow.files.ui
  (:require [re-frame.core :as rf]
            [goog.object :as gobj]))

(def <sub (comp deref re-frame.core/subscribe))
(def >evt re-frame.core/dispatch)


;;;;;;;;;;;;;;;
;; icon-view ;;
;;;;;;;;;;;;;;;


(defn dissoc-in
  "https://stackoverflow.com/a/14488425"
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (assoc m k newmap))
      m)
    (dissoc m k)))


(rf/reg-event-db
 ::delete
 (fn [db [_ {:keys [filename id type]}]]
   (dissoc-in db [id type filename])))

(defn file-view [{:keys [filename id type] :as opts}]
  (println "opts is " opts)
  [:div
   [:div>span.icon.is-large
    [:i.fab.fa-css3-alt.fa-3x]]
   [:div (cond-> filename
           (keyword? filename) name)
    [:span.icon.is-medium {:on-click #(rf/dispatch [::delete opts])}
     [:i.fas.fa-trash-alt]]]])

(rf/reg-sub ::files            
            (fn [db [_ id type]] (get-in db [id type])))

(defn file-list [{:keys [id type]}]
  (println "id type " id type)
  [:div.column>div.columns
   (cond->> @(rf/subscribe [::files id type])
     some? (map (fn [[n _]]
                  [:div.column {:key n} [file-view  {:filename n
                                                     :id id
                                                     :type type}]])))])


;;;;;;;;;;;;;;;;
;; input-view ;;
;;;;;;;;;;;;;;;;


(rf/reg-event-fx
 ::new
 (fn [{:keys [db]} [_ [type id {:keys [name file-content dispatch] :as m}]]]
   (cond-> {:db (assoc-in db [id type name] file-content)}
     (vector? dispatch) (merge {:dispatch [(first dispatch)
                                           (assoc (second dispatch) :file-data file-content)]}))))

(defn get-files [e]
  (array-seq (.. e -target -files)))

(defn read-file
  "read file and dispatch an event of [:file-content {:id :content}],
   will apply a process function if provided"
  [file  {:keys [id type process dispatch binary]}]
  (println "Trying to read file name " (.-name file) "and will dispatch" dispatch)
  (let [reader (js/FileReader.)]
    (gobj/set reader
              "onload"
              (fn [e]
                (println "event called ")
                (rf/dispatch [::new [type id {:file-content (cond-> (.. e -target -result)
                                                              (fn? process) process)
                                              :name (.-name file)
                                              :dispatch dispatch}]])))
    (if (true? binary)
      (.readAsDataURL reader file)
      (.readAsText reader file))))


(defn file-input [m]
  [:div.field.file.is-boxed>label.file-label
   [:div.field>div.control
    [:input.input.file-input
     {:type "file"
      :on-change (fn [e] (-> e get-files first (read-file m)))}]
    [:span.file-cta
     [:span.file-icon>i.fas.fa-upload]
     [:span.file-label (:placeholder m)]]]])

;;;;;;;;;;;;;;;;
;; combo view ;;
;;;;;;;;;;;;;;;;

(defn view [{:keys [id placeholder process type dispatch] :as m}]
  [:div.columns
   [:div.column [file-input m]]
   [:div.column [file-list m]]])
