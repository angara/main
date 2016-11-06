
(ns meteo.old-ws
  (:require
    ; [rum.server-render :refer [render-static-markup]]
    ; [mlib.conf :refer [conf]]
    ; [mlib.http :refer [make-url]]
    ; [mlib.web.snippets :refer [one-pix-src yandex-metrika mailru-top analytics]]
    ; [html.util :refer [ficon json-resp inner-html]]))
    [compojure.core :refer [GET ANY defroutes]]
    [compojure.route :refer [resources]]
    [html.frame :refer [html5]]))
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
;
; x.meteo_st = (req, res) ->
;     db.meteo_st().find()
;         {"last.ts": {"$gt": moment().subtract('hours',2).toDate()}, "pub":{"$gt":0}},
;         {_id:1, ts:1, last:1, ll:1, elev:1, title:1, addr:1, descr:1, url:1}
;     .toArray (err, data) -> res.json data
; #-
;
; x.st_series = (req, res) ->
;     db.meteo_st().findOne()
;         {_id:req.query.st, pub:{$gt:0}}
;         {_id:1, series:1}
;         (err, data) -> res.set(ACL_HDR).json(data)
;
; #-


(defn get-dat [req]
  (prn "dat.")
  "dat.text")
;

(defn get-st [req])
;

(defn get-series [req])
;


(defroutes routes
  (GET "/"          [] index)
  (GET "/dat"       [] get-dat)
  (GET "/st"        [] get-st)
  (GET "/st_series" [] get-series)
  (resources "/" {:root (str "public" BASE)}))
;

;;.
