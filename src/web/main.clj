;;;;
;;  angara-main
;;;;

(ns web.main
  (:gen-class)
  (:require
    [taoensso.timbre :refer [error]]
    [mount.core :refer [defstate start-with-args]]
    [mlib.conf :refer [conf]]
    [mlib.core :refer [edn-read]]
    [web.app]))
;

(defstate main
  :start
    identity
  :stop
    (when (-> conf :http :stop-uri)
      (info "exiting")
      (System/exit 0)))
;

(defn -main [& args]
  (if-let [rc (edn-read (first args))]
    (start-with-args rc)
    (error "config profile must be in parameters!")))
;

;;.
