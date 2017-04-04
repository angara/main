
(ns tourserv.core
  (:require
    [compojure.core :refer [defroutes GET POST]]
    [html.frame :refer [render]]
    [tourserv.db]
    [tourserv.html :refer [index-page serv-page]]))
;


(defroutes routes
  (GET "/"      [] index-page)
  (GET "/:type" [] serv-page))
;

;;.
