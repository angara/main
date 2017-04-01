
(ns meteo.old-ws
  (:require
    [clj-time.core :as tc]
    [mlib.log :refer [warn]]
    [monger.collection :as mc]
    [monger.query :as mq]
    [compojure.core :refer [GET ANY defroutes]]
    [compojure.route :refer [resources]]
    [mlib.http :refer [json-resp]]
    [html.frame :refer [html5]]
    [meteo.db :refer [db ST]]))
;

(def BASE "/meteo/old-ws")

(defn index [req]
  (html5
    [:html
      [:head
        [:title "Angara.Net/meteo - Погода в Иркутске, на Байкале и в Прибайкалье"]
        [:link { :rel "stylesheet" :type "text/css" :href (str BASE "/main.css")}]
        [:script {:src (str BASE "/jquery.min.js")}]
        [:script {:src (str BASE "/meteo.js")}]
        [:script {:src (str BASE "/ymap.js")}]]
      [:body.wh100
        [:div#mapdiv]]]))
;


; ACL_HDR = {"Access-Control-Allow-Origin": "*"}
;
; x.dat = (req, res) ->
;     db.meteo_st().find()
;         {"last.ts": {"$gt": moment().subtract('days',7).toDate()}}
;     .toArray (err, data) ->
;         res.set "Content-Type", "text/plain; charset=utf-8"
;         res.send JSON.stringify data, null, 2
; #-

(defn st-data []
  (let [fresh-ts (tc/minus (tc/now) (tc/hours 2))]
    (try
      (mq/with-collection (db) ST
        (mq/find {:last.ts {:$gt fresh-ts} :pub {:$gt 0}})
        (mq/fields [:_id :ts :last :ll :elev :title :addr :descr :url])
        (mq/sort (array-map :ts -1))
        (mq/limit 100))
      (catch Exception e (warn e)))))
  ;
;

(defn get-series [{{st :st} :params}]
  (->
    (try
      (mc/find-one-as-map (db) ST
        {:_id st :pub {:$gt 0}}
        [:_id :series])
      (catch Exception e (warn e)))
    (json-resp)
    (assoc-in [:headers "Access-Control-Allow-Origin"] "*")))
;


(defroutes routes
  (GET "/"          [] index)
  (GET "/dat"       [] (json-resp (st-data)))
  (GET "/st"        [] (json-resp (st-data)))
  (GET "/st_series" [] get-series)
  (resources "/" {:root (str "public" BASE)}))
;

;;.
