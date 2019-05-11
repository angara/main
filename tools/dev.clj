
(ns dev
  (:require
    [clojure.edn :as edn]
    [mount.core :refer [start-with-args start stop]]
    [mlib.log :refer [warn]]
    [mlib.conf :refer [conf]]
    [web.srv :refer [server]]))
;


(defn load-conf []
  (-> "../conf/dev.edn"
    (slurp)
    (edn/read-string)))
;

(defn restart []
  (stop)
  (start-with-args 
    (load-conf)))
;

(comment

  conf

  (restart)

  

  .)

;;.
