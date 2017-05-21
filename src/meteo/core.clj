
(ns meteo.core
  (:require
    [clj-time.core :as tc]
    [compojure.core :refer [defroutes GET POST]]
    ;
    ; [mlib.conf :refer [conf]]
    [mlib.core :refer [hesc]]
    ; [mlib.http :refer [json-resp]]))
    ;
    [meteo.db :refer [db st-ids]]
    ;
    [html.frame :refer [render-layout]]))
;

(def FRESH_INTERVAL (tc/minutes 60))

(defn fresh? [last]
  (try
    (tc/after? (:ts last) (tc/minus (tc/now) FRESH_INTERVAL))
    (catch Exception ignore)))
;

(defn graph-page [req]

  ;; user-prefs
  ;; graphr-prefs

  (render-layout req
    {:title "Погода в Иркутске - Графики"}
    [:div.jumbotron
      [:h2.text-center "В разработке"]
      [:p "Графики"]]))
;

(defn index-page [req]

  ;; TODO: user-prefs
  (let [ids ["npsd" "uiii" "lin_list" "olha"]
        sts (into {}
              (map
                (fn [st] [(:_id st) st])
                (st-ids ids)))]

    (render-layout req
      {:title "Погода в Иркутске"}
      [:div.b-meteo
        ;
        [:div.row
          (for [id ids
                :let [st (get sts id)]
                :when st]
            (let [title (:title st)
                  descr (:descr st (:addr st))
                  last  (:last st)
                  fresh (fresh? last)]
              [:div.col-sm-3
                [:div.b-card
                  [:div.st-name (hesc title)]
                  [:div.st-descr (hesc descr)]
                  [:div "fresh:" fresh]
                  [:div "last:" last]]]))]])))
;

(defroutes meteo-routes

  (GET "/"      [] index-page)
  (GET "/graph" [] graph-page))

;

;;.
