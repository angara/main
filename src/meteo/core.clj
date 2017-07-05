
(ns meteo.core
  (:require
    [clojure.string :as s]
    [clj-time.core :as tc]
    [clj-time.coerce :refer [to-long]]
    ;
    [compojure.core :refer [defroutes GET POST]]
    ;
    [mlib.conf :refer [conf]]
    [mlib.core :refer [hesc]]
    ; [mlib.http :refer [json-resp]]))
    ;
    [meteo.db :refer [st-ids st-find]]
    [meteo.fmt :refer [format-t format-h format-p format-w format-wt]]
    [meteo.util :refer [st-param ST_MAX_NUM fresh]]
    ;
    [html.frame :refer [render-layout]]))
;


(def ST_DEAD_INTERVAL (tc/days 10))
(def HOURS_INTERVAL (tc/hours 60))


(defn graph-page [req]

  ;; user-prefs
  ;; graphr-prefs

  (render-layout req
    { :title "Погода в Иркутске - Графики"
      :topmenu :meteo}
    [:div.jumbotron
      [:h2.text-center "В разработке"]
      [:p "Графики"]]))
;

(defn index-page [req]

  ;; TODO: user-prefs
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
        t1  (tc/floor now_tz tc/hour)
        t0  (tc/minus t1 HOURS_INTERVAL)
        t0-utc (tc/from-time-zone t0 tc/utc)
        idx (volatile! 0)]
    ;;

    (render-layout req
      { :title "Погода в Иркутске"
        :topmenu :meteo
        :css [ "//api.angara.net/incs/highcharts/5.0.12/highcharts.css"]
        :js  [ "//api.angara.net/incs/highcharts/5.0.12/highcharts.js"
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
            (let [title (:title st)
                  descr (:descr st (:addr st))
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
                      (hesc title)]
                    (if last
                      (list
                        [:div.t
                          (format-t (:t last) (-> trends :t :avg))]
                        [:div.wph
                          [:div.w
                            (format-w (:w last) (:g last) (:b last))]
                          [:div.p
                            (format-p (:p last))]
                          [:div.h
                            (format-h (:h last))]
                          [:div.wt
                            (format-wt (:wt last) (:wl last))]]
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

(defroutes meteo-routes

  (GET "/"      [] index-page)
  (GET "/graph" [] graph-page))

;

;;.
