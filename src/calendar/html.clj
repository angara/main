
(ns calendar.html
  (:require
    [mlib.log :refer [debug info warn]]
    [mlib.core :refer [hesc]]
    [mlib.time :refer [ddmmyyyy]]
    [mlib.web.snippets :refer [ya-rtb]]
    ;
    [html.frame :refer [render]]
    [calendar.db :refer [crecs-all]]))
;


(defn index-page [req]

  (render req
    {:page-title "Календарь событий"}

    [:div.b-calendar
      "calendar"]))
;

(defn all-page [req]
  (let [crecs (crecs-all)]
    ;
    (render req
      {:page-title "Календарь: все записи"}
      ;
      [:div.b-calendar
        (for [r crecs
              :let [publ (= "publ" (:status r))]]
          [:div.b-crec
            [:span {:class (if publ "c_publ" "c_none")}
              (ddmmyyyy (:date r))]
            " - "
            [:a {:href (:link r)}
              (hesc (:title r))]])])))
;

;;.
