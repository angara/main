
(ns user
  (:require
    [clojure.tools.namespace.repl :as tnr]
    [util :as util]
    ;
    [web.srv :refer [server]]))
;

(set! *warn-on-reflection* true)

(def _void server)

(defn restart []
  (prn "restart")
  (util/stop)
  (util/start))
;

(defn reset []
  (tnr/refresh :after 'user/restart))
;

(comment

  (restart)
  (reset)
    
  ,)

;;.
