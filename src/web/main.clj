;;;;
;;  angara-main
;;;;

(ns web.main
  (:require
    [taoensso.timbre :refer [error]]
    [mount.core :as mount]
    [mlib.conf :refer [conf run-conf]]
    [mlib.core :refer [edn-read]]
    [web.app])
  (:gen-class))
;


(defn -main [& args]
  (if (reset! run-conf (edn-read (first args)))
    (mount/start)
    (error "running config profile must be in parameters!")))
;

;;.
