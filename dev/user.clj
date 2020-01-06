
(ns user
  (:require
    [clojure.tools.namespace.repl :as tnr]
    [util :as util]
    ;
    [web.srv :refer [server]]
    ;
    [m :refer [meteo-db]]))
;

(set! *warn-on-reflection* true)

(def _void server)
(def _void1 meteo-db)

(defn restart []
  (prn "restart")
  (util/stop)
  (util/start))
;

(defn reset []
  (tnr/refresh :after 'user/restart))
;

(comment

  (reset)
  (restart)
  
  ,)

;;.
