
(ns calendar.my
  (:require
    [mlib.log :refer [debug info warn]]
    [mlib.core :refer [hesc]]
    ;
    [html.frame :refer [render]]))
;


(defn my-page [req]


  (render req
    {}

    [:div
      "calendar/my"]))
;
