
(ns meteo.core
  (:require
    [clojure.string :as s]
    [clj-time.core :as tc]
    [compojure.core :refer [defroutes GET POST]]
    ;
    ; [mlib.conf :refer [conf]]
    [mlib.core :refer [hesc]]
    ; [mlib.http :refer [json-resp]]))
    ;
    [meteo.db :refer [db st-ids st-find]]
    ;
    [html.frame :refer [render-layout]]))
;


(def ST_PARAM  :st)
(def ST_COOKIE "meteo_st")

(def ST_DEFAULT
  ["asbtv" "npsd" "uiii" "lin_list" "olha" "irgp" "irk2" "zbereg" "nicola"])
;

(def ST_DEAD_INTERVAL (tc/days 10))

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


(defn comma-split [s]
  (->>
    (s/split (s/lower-case (str s)) #"\,")
    (remove s/blank?)
    (not-empty)))
;

(defn st-param [req]
  (or
    (comma-split (-> req :params ST_PARAM))
    (comma-split (-> req :cookies (get ST_COOKIE) :value))
    ST_DEFAULT))
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
                    [:_id :title :descr :addr :ll])]
    ;;

    (render-layout req
      { :title "Погода в Иркутске"
        :topmenu :meteo
        :js ["/incs/meteo/core.js"]}
      ;
      [:div.b-meteo
        ;
        [:div.row
          (for [id ids
                :let [st (get sts id)]
                :when st]
            (let [title (:title st)
                  ;descr (:descr st (:addr st))
                  last    (fresh (:last st))
                  trends  (fresh (:trends st))]
              ;
              [:div.col-sm-4
                [:div.b-card
                  {:data-st (:_id st)}
                  [:div.title (hesc title)]
                  ; [:div.st-descr (hesc descr)]
                  (if last
                    (list
                      [:div.t
                        (format-t (:t last) (-> trends :t :avg))]
                      [:div.clearfix])
                    ;;
                    [:div.nodata "Нет данных."])]]))]
        [:div.col-sm-8.col-sm-offset-2
          [:div.form-inline
            [:div.form-group
              [:select#st_list.form-control
                (for [st st-names]
                  [:option {:value (:_id st)} (hesc (:title st))])]
              " &nbsp; "
              [:button#btn_st_add.btn.btn-success {:type "button"}
                [:b "Добавить"]]]]]])))



;

(defroutes meteo-routes

  (GET "/"      [] index-page)
  (GET "/graph" [] graph-page))

;

;;.
