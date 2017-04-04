
(ns tourserv.core
  (:require
    [compojure.core :refer [defroutes GET POST]]
    [html.frame :refer [render]]))
;


(defn index-page [req]

  (render {} req
    [:div.text-center {:style "margin: 10ex;"}
      "Раздел на реконструкции."]))
;


(defroutes routes
  (GET "/" [] index-page))
;

;;.
