(ns meteo.core
  (:require
   [clojure.math :refer [round]]
   [java-time.api :as jt]
    ;
   [ring.util.response :refer [redirect]]
    ;
   [mlib.core :refer [to-int hesc]]
   [mlib.http :refer [json-resp json-err]]
   [mlib.web.snippets :refer [ya-rtb]]   
    ;
   [meteo.fmt  :refer [format-t format-h format-p format-w]]
   [meteo.data :as mdata]
    ;
   [lib.rus-date :refer [RUS_MONTHS_FC]]
   [lib.time :refer [iso->local iso->inst local-now tf-hhmm tf-ddmmyyyy]]
   [html.frame :refer [render-layout]]
   ,))


(def YEAR_0 2001)

(def TZ_OFFSET_MILLIS (* 8 3600 1000)) ;; NOTE: should depend on conf/tz

(def ST_BASE_URL "/meteo/st/")


(defn fresh-local-ts? [ts]
  (when ts
    (jt/> ts (jt/minus (local-now) (jt/minutes 80)))))

(defn not-dead-local-ts? [ts]
  (when ts
    (jt/> ts (jt/minus (local-now) (jt/minutes 240)))))

; ; ; ; ; ; ; ; ; ;

(defn st-hourly [{{:keys [st ts_beg ts_end]} :params}]
  (try
    (let [_ (when-not st (throw (Exception. (str "incorrect parameter st:" st))))
          ts-beg (iso->inst ts_beg)
          _ (when-not ts-beg (throw (Exception. (str "incorrect parameter ts_beg:" ts_beg))))
          ts-end (iso->inst ts_end)
          _ (when-not ts-end (throw (Exception. (str "incorrect parameter ts_end:" ts_end))))
          ts-beg (jt/truncate-to ts-beg :hours)
          ts-end (jt/truncate-to ts-end :hours)
          hrs (jt/time-between ts-beg ts-end :hours)
          _ (when (or (< hrs 1) (> hrs 744))
              (throw (Exception. (str "incorrect ts hour range:" hrs))))
          ;
          resp (mdata/st-hourly st ts-beg ts-end)
          ]
      (json-resp resp))
    (catch Exception ex
      (json-err {:error (ex-message ex)}))
    ,))

; ; ; ; ; ; ; ; ; ;

(defn st-page [{params :params :as req}]
  (when-let [st-info (mdata/st-info (:st params))]  
    (let [now (local-now)
          now-ms (System/currentTimeMillis)
          yr  (-> params :year  (to-int 0))
          mn  (-> params :month (to-int 0))
            ;
          year0 (some-> (:created_at st-info) (iso->local) (.getYear))
          year1 (or
                 (some-> (:closed_at st-info) (iso->local) (.getYear))
                 (.getYear now)
                 )
          ;
          year  (if (= 0 yr) 
                  (.getYear now)
                  (max (or year0 YEAR_0) 
                       (min yr (or year1 (.getYear now)))))
            ;
          month (if (= 0 mn)
                  (.getMonthValue now)
                  (max 1 (min mn 12)))
          ]
      ;
      (if (or (not= yr year) (not= mn month))
        (redirect (str ST_BASE_URL (:st st-info) "?year=" year "&month=" month))
          ;;
        (let [{:keys [title descr elev last last_ts]} st-info
              last-ts (iso->local last_ts)
              dead   (not (fresh-local-ts? last-ts))]
            ;
          (render-layout req
                         {:title (str "Погода - " (:title st-info))
                          :topmenu :meteo
                          :css [ "/incs/highcharts/5.0.14/highcharts.css"]
                          :js  [ "/incs/highcharts/5.0.14/highcharts.js" "/incs/meteo/st_graph.js"]}
              ;;
                         [:div.b-meteo.row
                          [:script
                           "window.st_id='" (:st st-info) "';"
                           "window.st_month=" month ";"
                           "window.st_year="  year  ";"
                           "window.tz_offset_millis=" TZ_OFFSET_MILLIS ";"
                           "window.now_ms=" now-ms ";"]
                ;
                          [:div {:class (if dead "b-st dead" "b-st")}
                           [:div.col-md-12
                            [:div.title title]
                            (when descr [:div.descr descr])
                            (when elev [:div.elev "высота " [:b (round elev)] " м"])
                            (when last-ts
                              [:div.date (tf-ddmmyyyy last-ts) " - " (tf-hhmm last-ts)])
                            (when dead [:div.dead-msg "Данные устарели!"])]
                           
                           [:div.clearfix]

                           [:div.col-md-7.col-md-offset-2
                            [:div.twph
                             (when (not-dead-local-ts? (iso->local (:t_ts last)))
                               [:div.t (format-t "<span class='lbl'>Температура:</span> "
                                                 (:t last) (-> last :t_delta))])
                             (when (not-dead-local-ts? (iso->local (:w_ts last)))
                               [:div.w (format-w "<span class='lbl'>Ветер:</span> "
                                                 (:w last) (:g last) (:b last))])
                             (when (not-dead-local-ts? (iso->local (:p_ts last)))
                               [:div.p (format-p "<span class='lbl'>Давление:</span> " (:p last))])
                             (when (not-dead-local-ts? (iso->local (:h_ts last)))
                               [:div.h (format-h "<span class='lbl'>Влажность:</span> " (:h last))])
                             ]]
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
                            [:div#st_graph.st-graph]]]]))))))
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
         (hesc title) " " [:i.fa.fa-bar-chart] [:div.hhmm (when ts (tf-hhmm ts))]]]
       (labeled-value "температура" t-val)
       (labeled-value "ветер"       w-val)
       (labeled-value "давление"    p-val)
       (labeled-value "влажность"   h-val)
       [:div.descr (hesc descr) (when elev (list " ^ " [:b (round elev)] "м"))]
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
        [:div.text-center {:style "font-size: 120%;"}
         "Данные на карте: " [:a {:target "_blank" :href "https://meteo38.ru/map"} "meteo38.ru/map"]
         [:br]
         "Бот в Телеграм: " [:a {:target "_blank" :href "https://t.me/meteo38bot"} "@meteo38bot"]
         ]
        [:div.text-center {:style "margin: 20px"}
          [:small {:style "color:#777"}
            "Данные, приведенные на этой странице, не являются официальными"
            " и не могут быть использованы в качестве документальных."]]])))

