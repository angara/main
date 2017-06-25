
(ns meteo.core
  (:import
    [java.util Locale])
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
    [meteo.db :refer [db st-ids st-find]]
    ;
    [html.frame :refer [render-layout]]))
;


(def ST_PARAM  :st)
(def ST_COOKIE "meteo_st")

(def ST_MAX_NUM 100)

(def ST_DEAD_INTERVAL (tc/days 10))

(def HOURS_INTERVAL (tc/hours 72))

(def FRESH_INTERVAL (tc/minutes 60))

(defn fresh [data]
  (try
    (and
      (tc/after?
        (:ts data)
        (tc/minus (tc/now) FRESH_INTERVAL))
      data)
    (catch Exception ignore)))
;

;;; ;;; ;;; ;;;

(def HPA_MMHG 1.3332239)


(defn nf1 [x]
  (String/format Locale/ROOT "%.1f" (to-array [(float x)])))
;

(defn nf2 [x]
  (String/format Locale/ROOT "%.2f" (to-array [(float x)])))
;

(defn wind-nesw [b]
  (try
    (get
      ["С","СВ","В","ЮВ","Ю","ЮЗ","З","СЗ"]
      (int (Math/floor (mod (/ (+ b 22) 45) 8))))
    (catch Exception ignore)))
;

(defn format-w [w g b]
  (when w
    (let [res (str "Ветер: " "<b>" (Math/round (float w)) "</b>")
          res (if g
                (str res "<b>-" (Math/round (float g)) "</b>" " м/с")
                (str res " м/с"))
          res (str "<nobr>" res "</nobr>")
          dir (wind-nesw b)]
      (if dir
        (str res ", " "<b>" dir "</b>")
        res))))
;

(defn format-h [h]
  (when h
    (str "Влажность: <nobr><b>" (Math/round (float h)) "</b> %</nobr>")))
;

(defn format-p [p]
  (when p
    (str "Давление: <nobr><b>"
      (Math/round (/ p HPA_MMHG))
      "</b> мм.рт.ст</nobr>")))
;

(defn format-t [t avg]
  (try
    (let [t (Math/round (float t))
          [cls sign]  (cond
                        (< 0 t) ["pos" "+"]
                        (> 0 t) ["neg" "-"]
                        :else   ["zer" ""])
          [trc arr]   (when avg
                        (cond
                          (> t (+ avg 1)) ["pos" "&uarr;"]
                          (< t (- avg 1)) ["neg" "&darr;"]
                          :else           [""    "&nbsp;"]))]
      (list
        [:span {:class cls} sign [:i (Math/abs t)]]
        "&deg;"
        [:span {:class (str "arr " trc)} arr]))
      ;
    (catch Exception ignore)))
;

(defn format-wt [wt wl]
  (try
    (when wt
      (str "Температура воды: <nobr><b>"
            (Math/round (float wt)) "</b>&deg;</nobr>"
        (when wl
          (str ", <nobr>уровень <b>" (nf2 wl) "</b> м</nobr>"))))
    (catch Exception ignore)))
;


(defn comma-split [s]
  (->>
    (s/split (s/lower-case (str s)) #"\,")
    (remove s/blank?)
    (not-empty)))
;

(defn st-param [req]
  (take ST_MAX_NUM
    (or
      (comma-split (-> req :params ST_PARAM))
      (comma-split (-> req :cookies (get ST_COOKIE) :value))
      (-> conf :meteo :st_default))))
;


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
        ;;t1  (tc/plus (tc/floor now_tz tc/day) (tc/days 1))
        t1  (tc/floor now_tz tc/hour)
        t0  (tc/minus t1 HOURS_INTERVAL)]
    ;;

    (render-layout req
      { :title "Погода в Иркутске"
        :topmenu :meteo
        :js ["/incs/meteo/core.js"]}
      ;
      [:div.b-meteo
        [:script
          "window.hourly_t0=new Date(" (to-long t0) ");"
          "window.hourly_t1=new Date(" (to-long t1) ");"]
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
                    [:div.nodata "Нет данных."])]]))]
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
        ;
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
