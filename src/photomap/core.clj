
(ns photomap.core
  (:require
    [taoensso.timbre :refer [debug info warn]]
    [compojure.core :refer [GET POST ANY defroutes]]
    [compojure.route :refer [files]]
    [mlib.conf :refer [conf]]
    [html.frame :refer [layout]]
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
      (for [p (db/hist)]
        [:div
          [:img {:src (photo-uri (:hash p) "_xs.jpg")}]
          (str (:user p))])]))
;

(defroutes routes
  (files JPG {:root (-> conf :photomap :hash-dir)})
  (GET "/list" [] photo-list))
;

;;.
