(ns meteo.core
  (:require
    [clojure.math :refer [round]]
    [clj-time.core :as tc]
    [clj-time.coerce :as tco]
    ;
    [ring.util.response :refer [redirect]]
    ;
    [mlib.core :refer [hesc to-int]]
    [mlib.time :refer [ddmmyyyy]]
    [mlib.web.snippets :refer [ya-rtb]]   
    ;
    [meteo.db   :refer [st-pub hourly-ts0 hourly-ts1]]
    [meteo.fmt  :refer [format-t format-h format-p format-w format-wt]]
    [meteo.util :refer [fresh]]
    [meteo.data :as mdata]
    ;
    [lib.rus-date :refer [RUS_MONTHS_FC]]
    [lib.time :refer [iso->local tf-hhmm]]
    [html.frame :refer [render-layout]]
   ,))


(def YEAR_0 2013)

(def ST_DEAD_INTERVAL (tc/days 10))
(def HOURS_INTERVAL   (tc/hours 60))
(def TZ_OFFSET_MILLIS (* 8 3600 1000))

(def ST_BASE_URL "/meteo/st/")

(defn st-url [st]
  (str ST_BASE_URL (:_id st)))


(defn st-hourly [{:keys [st ts_beg ts_end]}]
  (let [
        
        ]
    
    )
  )


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
                dead   (not (fresh last))]
            ;
            (render-layout req
              { :title (str "Погода - " (:title st))
                :topmenu :meteo
                :css [ "/incs/highcharts/5.0.14/highcharts.css"]
                :js  [ "/incs/highcharts/5.0.14/highcharts.js" "/incs/meteo/st_graph.js"]}
              ;;
              [:div.b-meteo.row
                [:script
                  "window.st_id='" (:_id st) "';"
                  "window.st_month=" month ";"
                  "window.st_year="  year  ";"
                  "window.tz_offset_millis=" TZ_OFFSET_MILLIS ";"
                  "window.now_ms=" (tco/to-long now) ";"]
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
                        (ddmmyyyy t) " - " #_(hhmm t)]) ;; XXX: !!!
                    (when dead
                      [:div.dead-msg "Данные устарели!"])]
                  [:div.clearfix]
                  [:div.col-md-7.col-md-offset-2
                    [:div.twph
                      [:div.t
                        (format-t "<span class='lbl'>Температура:</span> "
                          (:t last) (-> last :t_delta))]
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
                          (when (and year0 year1)
                            (for [y (range year0 (inc year1))]
                              [:option {:value y} y]))
                         ]]
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

; ; ; ; ; ; ; ; ;

(defn- labeled-value [lbl val]
  (when val
    [:div.value.clearfix
     [:div.col-sm-6.lbl lbl]
     [:div.col-sm-6.val val]]))


(defn format-station [{st :st title :title descr :descr
                       last :last last_ts :last_ts
                       lat :lat lon :lon elev :elev }]
  (let [t-val (format-t nil (:t last) (:t_delat last))
        w-val (format-w nil (:w last) (:g last) (:b last))
        p-val (format-p nil (:p last))
        h-val (format-h nil (:h last))
        ts (iso->local last_ts)
        a-title (str st (when (and lat lon) (str ": " lat "/" lon)))
        ]
    (when (or t-val w-val p-val h-val)
      [:div.station
       [:div.title 
        [:a {:href (str ST_BASE_URL st) :title a-title} 
         title " " [:i.fa.fa-bar-chart] [:div.hhmm (when ts (tf-hhmm ts))]]]
       (labeled-value "температура" t-val)
       (labeled-value "ветер"       w-val)
       (labeled-value "давление"    p-val)
       (labeled-value "влажность"   h-val)
       [:div.descr descr (when elev (list " ^ " [:b (round elev)] "м"))]
       [:div.clearfix]]
      ,)))


(defn index-page [req]
  (let [st-list (mdata/active-stations)]
    (render-layout req
      { :title "Погода в Иркутске и Прибайкалье в реальном времени"
        :topmenu :meteo
        ;; :css ["/incs/highcharts/5.0.14/highcharts.css"]
        ;; :js ["/incs/meteo/core.js"]
       }
      [:div.b-meteo
       [:div.b-meteo-brief
        [:div.col-md-7.col-md-offset-1
         (for [st st-list]
           (format-station st))]
        [:div.col-md-4 {:style "text-align: center"}
         [:div {:style "width: 300px; height: 300px; margin: 12px 8px; display: inline-block; overflow: hidden;"}
          (ya-rtb "R-A-1908-16" true)]]
        [:div.clearfix]]
        ;;
        [:div.clearfix]
        [:div.text-center {:style "margin: 20px"}
          [:small {:style "color:#777"}
            "Данные, приведенные на этой странице, не являются официальными"
            " и не могут быть использованы в качестве документальных."]]])))

