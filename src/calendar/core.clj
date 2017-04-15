
(ns calendar.core
  (:require
    [compojure.core :refer [defroutes GET POST]]  ;context ANY
    [mlib.http :refer [json-resp]]
    ;
    [calendar.html :refer [index-page]]))
;

(defroutes calendar-routes
  (GET "/" [] index-page))
  ; (ANY      "/*"      _ (json-resp {:err :req})))
;

;;
