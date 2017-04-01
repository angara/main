
(ns photomap.core
  (:require
    [mlib.log :refer [debug info warn]]
    [compojure.core :refer [GET POST routes]]
    [compojure.route :refer [files]]
    [mlib.conf :refer [conf]]
    [mlib.http :refer [json-resp]]
    [mlib.time :refer [ddmmyy-hhmm]]
    [html.frame :refer [layout html5 head]]
    [photomap.db :as db]))
;


(def JPG "/jpg")

(defn photo-uri [hash & [suff]]
  (when hash
    (let [base (-> conf :photomap :base-uri)]
      (str base JPG "/" (subs hash 0 2) "/" (subs hash 2 4)
                "/" hash (or suff ".jpg")))))
;


(defn photo-list [req]
  (layout req {}
    [:div
      (for [p (db/hist) :let [hash (:hash p)] :when hash]
        [:div
          [:div "timestamp: " (:ts p)]
          [:a {:href (photo-uri hash ".jpg")}
            [:img {:src (photo-uri hash "_xs.jpg")}]]
          [:div
            (str (:user p))]
          [:div
            (str (:chat p))]])]))
;

(defn photomap [req]
  (let [google-key (-> conf :photomap :google-key)
        gapi "https://maps.googleapis.com/maps/api/js?key="
        gopts "&callback=init_gmap&language=ru"]
    (html5
      [:html
        (head req {:js ["/incs/photomap/gmap.js"]})
        [:body
          {:style {:width "100vw" :height "100vh"}}
          [:div#gmap
            {:style
              {:background "#ddd" :width "100vw" :height "100vh"}}
            [:div {:style {:color "#777" :padding "10px 20px"}}
              "Загрузка карты ..."]]
          [:script
            { :async 1 :defer 1
              :src (str gapi google-key gopts)}]]])))
;

(defn photo->marker [pho]
  (let [hash (:hash pho)
        coord {:lat (-> pho :ll second) :lng (-> pho :ll first)}]
    (assoc
      (select-keys pho [:hash :caption :ts :chat :user])
      :id (:_id pho)
      :coord coord
      :date (ddmmyy-hhmm (:ts pho))
      :ico  (photo-uri hash "_xs.jpg")
      :pict (photo-uri hash "_md.jpg")
      :orig (photo-uri hash ".jpg"))))
;

(defn markers [req]
  (let [photos (db/hist)]
    (json-resp {:ok 1 :markers (map photo->marker photos)})))
;

(defn make-routes []
  (routes
    (files JPG {:root (-> conf :photomap :hash-dir)})
    (GET "/markers" [] markers)
    (GET "/list" [] photo-list)
    (GET "/:alias" [] photomap)
    (GET "/"       [] photomap)))
;

;;.
