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
 (fn [db [_ {:keys [filename key]}]]
   (println "keys are " filename key)
   (dissoc-in db [::files key filename])))

(defn file-view [{:keys [filename key] :as opts}]
  (println "opts is " opts)
  [:div
   [:div>span.icon.is-large
    [:i.fab.fa-css3-alt.fa-3x]]
   [:div (name filename)
    [:span.icon.is-medium {:on-click #(rf/dispatch [::delete opts])}
     [:i.fas.fa-trash-alt]]]])

(rf/reg-sub ::all-files (fn [db [_ _]] (-> db ::files)))

(rf/reg-sub ::files
            (fn [_ _] (rf/subscribe [::all-files]))
            (fn [files [_ k]] (get files k)))

(defn file-list [k]
  (println "key is " k)
  [:div.column>div.columns
   (for [n  (-> [::files k] <sub keys)]
     [:div.column {:key n} [file-view  {:filename n
                                        :key k}]])])


;;;;;;;;;;;;;;;;
;; input-view ;;
;;;;;;;;;;;;;;;;

(rf/reg-event-db
 ::new
 (fn [db [_ [id {:keys [file-content name]}]]]
   (assoc-in db [::files id name] file-content)))

(defn get-files [e]
  (array-seq (.. e -target -files)))

(defn read-file
  "read file and dispatch an event of [:file-content {:id :content}]"
  [file id]
  (println "Trying to read file name " (.-name file))
  (let [reader (js/FileReader.)]
    (gobj/set reader
              "onload"
              (fn [e]
                (println "event called ")
                (rf/dispatch [::new [id {:file-content (.. e -target -result)
                                         :name (.-name file)}]])))
    (.readAsText reader file)))


(defn file-input [{:keys [placeholder id]}]
  [:div.field.file.is-boxed>label.file-label
   [:div.field>div.control
    [:input.input.file-input
     {:type "file"
      :on-change (fn [e] (-> e get-files first (read-file id)))}]
    [:span.file-cta
     [:span.file-icon>i.fas.fa-upload]
     [:span.file-label placeholder]]]])

;;;;;;;;;;;;;;;;
;; combo view ;;
;;;;;;;;;;;;;;;;

(defn view [{:keys [id placeholder] :as m}]
  [:div.columns
   [:div.column [file-input m]]
   [:div.column [file-list id]]])
