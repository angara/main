

(ns tourserv.html
  (:require
    [mlib.log :refer [debug info warn]]
    ;
    [html.frame :refer [render]]
    [tourserv.db :refer [tourserv-by-type]]))
;

(def TYPES
    #{"apart" "auto" "equip" "guide"})


(defn serv-page [{params :params :as req}]
  (let [type (:type params)]
    (when (get TYPES type)
      (render req
        { :title "Турсервис"
          :page-title "Турсервис"}
        ;
        [:div.text-center {:style "margin: 10ex;"}
          "Раздел на реконструкции - "
          type]))))
;


(defn index-page [req]
  (render req
    { :title "Турсервис"
      :page-title "Турсервис"}
    ;
    [:div.text-center {:style "margin: 10ex;"}
      "Раздел на реконструкции."]))
;


;;.
