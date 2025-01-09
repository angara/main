
(ns calendar.html
  (:require
    [clj-time.core :as tc]
    [hiccup.core :refer [html]]
    [ring.util.response :refer [header]]
    ;
    [mlib.logger :refer [warn]]
    [mlib.core :refer [hesc]]
    [mlib.http :refer [html-resp]]
    [mlib.time :refer [ddmmyyyy]]
    ;
    [html.frame :refer [render-layout]]
    [lib.rus-date :refer [rus-date]]
    [calendar.db :refer [crecs-all crecs-publ]]
   ,))


(def FRONT_BLOCK_LIMIT 5)


(defn get-month [cr]
  (try
    (-> cr :date tc/month)
    (catch Exception e
      (warn "crec:" (:_id cr) (.getMessage e)))))
;

(defn index-page [req]
  (let [crecs (crecs-publ)]
    ;
    (render-layout req
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
    (render-layout req
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

(defn front-block [_req]
  (let [crecs (take FRONT_BLOCK_LIMIT (crecs-publ))]
    (->
      [:div.b-calendar-block
        (for [r crecs
              :let [dt (rus-date (:date r))
                    title (:title r)
                    _img (:thumb r)
                    url (:link r)]]
          [:div.crec
            [:a {:href url}
              [:span.date (first dt) " " (second dt)]]
            " "
            [:a {:href url}
              [:span.title (hesc title)]]])
        [:div.more
          [:a {:href "/calendar"} ". . . &raquo;&raquo;"]]]
      ;; div
      (html)
      (html-resp)
      (header "Access-Control-Allow-Origin" "*"))))
;

;;.
