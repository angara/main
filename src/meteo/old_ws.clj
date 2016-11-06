
(ns meteo.old-ws
  (:require
    [rum.server-render :refer [render-static-markup]]
    ; [mlib.conf :refer [conf]]
    ; [mlib.http :refer [make-url]]
    ; [mlib.web.snippets :refer [one-pix-src yandex-metrika mailru-top analytics]]
    ; [html.util :refer [ficon json-resp inner-html]]))
    [compojure.core :refer [GET defroutes]]
    [compojure.route :refer [resources]]))
;


(defn index [req]
  (prn "index:" req))

  ; (render-static-markup)
  ;
  ; doctype html
  ; html
  ;   head
  ;     title
  ;         block title
  ;             | Angara.ws - Погода в Иркутске, на Байкале и в Прибайкалье
  ;
  ;     block css
  ;         link(rel='stylesheet', href='#{BASE_URI}inc/main.css')
  ;
  ;     block scripts
  ;         script(src="#{BASE_URI}inc/jquery.min.js")
  ;         script(src="#{BASE_URI}inc/meteo.js")
  ;         script(src="#{BASE_URI}inc/ymap.js")
  ;
  ;   body.wh100
  ;     div#mapdiv)

;

(defn get-dat [req])
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
  (resources "/"))
;

;;.
