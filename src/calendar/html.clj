
(ns calendar.html
  (:require
    [clojure.string :refer [lower-case]]
    [clj-time.core :as tc]
    [mlib.log :refer [debug info warn]]
    [mlib.core :refer [hesc]]
    [mlib.time :refer [ddmmyyyy]]
    [mlib.web.snippets :refer [ya-rtb]]
    ;
    [html.frame :refer [render]]
    [misc.util :refer [rus-date]]
    [calendar.db :refer [crecs-all crecs-publ]]))
;


(defn get-month [cr]
  (try
    (-> cr :date tc/month)
    (catch Exception e
      (warn "crec:" (:_id cr) (.getMessage e)))))
;

(defn index-page [req]
  (let [crecs (crecs-publ)]
    ;
    (render req
      { :page-title "Календарь событий"
        :topmenu :calendar}
      ;
      [:div.b-calendar
        (for [crecs-month (partition-by get-month crecs)]
          (list
            [:div.row
              (for [r crecs-month
                    :let [dt (rus-date (:date r))
                          img (:thumb r)
                          url (:link r)]]
                [:div.col-sm-4
                  [:div.b-card
                    [:div.date (first dt) " " (second dt)]
                    (when img
                      [:a {:href url}
                        [:img.thumb {:src img}]])
                    [:a.title {:href url}
                      (hesc (:title r))]
                    [:div.clearfix]]])
              [:div.clearfix]]
            ;; row
            [:hr]))])))
;

(defn all-page [req]
  (let [crecs (crecs-all)]
    ;
    (render req
      { :page-title "Календарь: все записи"
        :topmenu :calendar}
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
