
(ns calendar.html
  (:require
    [mlib.log :refer [debug info warn]]
    [mlib.core :refer [hesc]]
    [mlib.web.snippets :refer [ya-rtb]]
    ;
    [html.frame :refer [render]]))
;


(defn index-page [req]

  (render req
    {}

    [:div
      "calendar"]))
;

;;.
