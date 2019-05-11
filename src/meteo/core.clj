
(ns meteo.core
  (:require
    [clojure.string :as s]
    [clj-time.core :as tc]
    [clj-time.coerce :refer [to-long]]
    ;
    [ring.util.response :refer [redirect]]
    [compojure.core :refer [defroutes GET POST]]
    ;
    [mlib.conf :refer [conf]]
    [mlib.core :refer [hesc to-int]]
    [mlib.time :refer [ddmmyyyy hhmm]]
    ; [mlib.http :refer [json-resp]]))
    ;
    [meteo.db :refer [st-ids st-find st-pub hourly-ts0 hourly-ts1]]
    [meteo.fmt :refer [format-t format-h format-p format-w format-wt]]
    [meteo.util :refer [st-param ST_MAX_NUM fresh]]
    [meteo.graph :refer [t3-svg]]
    ;
    [misc.util :refer [RUS_MONTHS_FC]]
    [html.frame :refer [render-layout]]))
;


(def YEAR_0 2013)

(def ST_DEAD_INTERVAL (tc/days 10))
(def HOURS_INTERVAL (tc/hours 60))
(def TZ_OFFSET_MILLIS (* 8 3600 1000))

(def ST_BASE_URL "/meteo/st/")

(defn st-url [st]
  (str ST_BASE_URL (:_id st)))
;


(defn st-page [{params :params :as req}]
  (let [st-id (-> params :st str)]
    (when-let [st (st-pub st-id)]  
      (let [now (tc/now)
            yr  (-> params :year  (to-int 0))
            mn  (-> params :month (to-int 0))
            ;
            year0 (when-let [ts0 (hourly-ts0 (:_id st))]
                    (tc/year ts0))
            year1 (when-let [ts1 (hourly-ts1 (:_id st))]
                    (tc/year ts1))
            ;
            year  (if (= 0 yr) 
                    (tc/year now)
                    (max (or year0 YEAR_0) 
                      (min yr (or year1 (tc/year now)))))
            ;
            month (if (= 0 mn)
                    (tc/month now)
                    (max 1 (min mn 12)))]
        ;
        (if (or (not= yr year) (not= mn month))
          (redirect (str ST_BASE_URL st-id "?year=" year "&month=" month))
          ;;
          (let [last   (:last st)
                trends (-> st :trends fresh)
                dead   (not (fresh last))]
            ;
            (render-layout req
              { :title (str "Погода - " (:title st))
                :topmenu :meteo
                :css [ "//api.angara.net/incs/highcharts/5.0.14/highcharts.css"]
                :js  [ "//api.angara.net/incs/highcharts/5.0.14/highcharts.js"
                        "/incs/meteo/st_graph.js"]}
              ;;
              [:div.b-meteo.row
                [:script
                  "window.st_id='" (:_id st) "';"
                  "window.st_month=" month ";"
                  "window.st_year="  year  ";"
                  "window.tz_offset_millis=" TZ_OFFSET_MILLIS ";"
                  "window.now_ms=" (to-long now) ";"]
                ;
                [:div
                  (if dead
                    {:class "b-st dead"}
                    {:class "b-st"})
                  [:div.col-md-12
                    [:div.title (-> st :title hesc)]
                    (when-let [d (:descr st)]
                      [:div.descr (hesc d)])
                    (when-let [a (:addr  st)]
                      [:div.addr  (hesc a)])
                    (let [t (:ts last)]
                      [:div.date
                        (ddmmyyyy t) " - " (hhmm t)])
                    (when dead
                      [:div.dead-msg "Данные устарели!"])]
                  [:div.clearfix]
                  [:div.col-md-7.col-md-offset-2
                    [:div.twph
                      [:div.t
                        (format-t "<span class='lbl'>Температура:</span> "
                          (:t last) (-> trends :t :avg))]
                      [:div.w
                        (format-w "<span class='lbl'>Ветер:</span> "
                          (:w last) (:g last) (:b last))]
                      [:div.p
                        (format-p "<span class='lbl'>Давление:</span> "
                          (:p last))]
                      [:div.h
                        (format-h "<span class='lbl'>Влажность:</span> "
                          (:h last))]
                      [:div.wt
                        (format-wt "<span class='lbl20'>Температура воды:</span> "
                          (:wt last) (:wl last))]]]
                  [:div.col-md-3]
                    ;"right pane"]
                  [:div.clearfix]
                  ;; /st
                  [:div.col-md-12
                    [:div.months.row
                      [:div.col-sm-2
                        [:select.j_year.form-control
                          (for [y (range year0 (inc year1))]
                            [:option {:value y} y])]]
                      [:div.clearfix]
                      [:div.col-sm-12
                        (for [[i mon] (map-indexed vector RUS_MONTHS_FC)]
                          [:button.btn.btn-default.j_month {:data-month (inc i)} mon])]
                      ;;
                      [:div.clearfix]]
                    ;; /year:months
                    [:div#st_graph.st-graph]]]])))))))
                    ;  [:div.loading "Загрузка графика ..."]]]]])))))))
                ;; b-st
    ;;
;

(defn index-page [req]
  (let [ids (st-param req)
        sts (into {}
              (map
                (fn [st] [(:_id st) st])
                (st-ids ids)))
        dead-time (tc/minus (tc/now) ST_DEAD_INTERVAL)
        st-names (st-find
                    {:pub 1 :ts {:$gte dead-time}}
                    [:_id :title :descr :addr :ll])
        now_tz (tc/to-time-zone (tc/now) (tc/time-zone-for-id (:tz conf)))
        t1  (tc/plus (tc/floor now_tz tc/hour) (tc/hours 1))
        t0  (tc/minus t1 HOURS_INTERVAL)
        t0-utc (tc/from-time-zone t0 tc/utc)
        idx (volatile! 0)]
    ;;

    (render-layout req
      { :title "Погода в Иркутске и Прибайкалье в реальном времени"
        :topmenu :meteo
        :css [ "//api.angara.net/incs/highcharts/5.0.14/highcharts.css"]
        :js  [ "//api.angara.net/incs/highcharts/5.0.14/highcharts.js"
               "/incs/meteo/core.js"]}
      ;
      [:div.b-meteo
        [:script
          "window.hourly_t0=new Date(" (to-long t0) ");"
          "window.hourly_t1=new Date(" (to-long t1) ");"
          "window.hourly_t0_utc=new Date(" (to-long t0-utc) ");"]
        ;
        [:div.row
          (for [id ids
                :let [st (get sts id)]
                :when st]
            (let [title   (:title st)
                  descr   (:descr st (:addr st))
                  last    (fresh (:last st))
                  trends  (fresh (:trends st))]
              ;
              (list
                (when (= 0 (mod @idx 2))
                  [:div.clearfix])
                ;
                [:div.col-md-6
                  [:div.b-card
                    {:data-st (:_id st)}
                    [:div.title
                      {:title descr}
                      [:a {:href (st-url st)}
                        (hesc title)]]
                    (if last
                      (list
                        [:div.t
                          (format-t "" (:t last) (-> trends :t :avg))]
                        [:div.wph
                          [:div.w
                            (format-w "Ветер: "
                              (:w last) (:g last) (:b last))]
                          [:div.p
                            (format-p "Давление: " (:p last))]
                          [:div.h
                            (format-h "Влажность: " (:h last))]
                          [:div.wt
                            (format-wt "Температура воды: "
                              (:wt last) (:wl last))]]
                        [:div.clearfix])
                      ;;
                      [:div.nodata "Нет данных."])
                    [:div.clearfix]
                    [:div.graph
                      {:id (str "graph_" (vswap! idx inc) "_" id)}
                      [:div.loading "Загрузка графика ..."]]]])))]
        ;
        (when (< (count ids) ST_MAX_NUM)
          [:div.col-sm-6.col-sm-offset-3.selector
            [:div.form-inline
              [:div.form-group
                [:select#st_list.form-control
                  (for [st st-names]
                    [:option {:value (:_id st)} (hesc (:title st))])]
                " &nbsp; "
                [:button#btn_st_add.btn.btn-success {:type "button"}
                  [:b "Добавить"]]]]])
        ;;
        [:div.clearfix]
        [:div.text-center {:style "margin: 20px"}
          [:small {:style "color:#777"}
            "Данные, приведенные на этой странице, не являются официальными"
            " и не могут быть использованы в качестве документальных."]]])))
;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn graph-page [req]
  (render-layout req
    { :title "Погода в Иркутске и Прибайкалье в реальном времени"
      :topmenu :meteo}
    ;
    [:div.b-meteo
      [:b "SVG image"]
      [:br]
      [:img {:src "graph/t3.svg?st=uiii" :style "width:40%;"}]]))
;
  
;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(defroutes meteo-routes
  (GET "/"              [] index-page)

  (GET "/graph"         [] graph-page)

  (GET "/graph/t3.svg"  [] t3-svg)
  (GET "/st/:st"        [] st-page))
;

;;.
