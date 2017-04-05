
(ns tourserv.core
  (:require
    [compojure.core :refer [defroutes GET POST]]
    [html.frame :refer [render]]
    [tourserv.db]
    [tourserv.html :refer [index-page serv-apart serv-town serv-page]]))
;


(defroutes routes
  (GET "/"            [] index-page)
  (GET "/apart"       [] serv-apart)
  (GET "/apart/:town" [] serv-town)
  (GET "/:type"       [] serv-page))
;

;;.
