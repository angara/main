;;;;
;;  angara-main
;;;;

(ns web.main
  (:gen-class)
  (:require
    [clojure.edn :as edn]
    [taoensso.timbre :refer [info error]]
    [mount.core :refer [defstate start-with-args]]
    [mlib.conf :refer [conf]]
    [web.srv]))
;

; (defstate main
;   :start
;     identity)
;   ; :stop
  ;   (when (-> conf :http :stop-uri)
  ;     (info "exiting")
  ;     (System/exit 0)))
;

(defn -main [& args]
  (if-let [rc (-> args first slurp edn/read-string)]
    (start-with-args rc)
    (error "config profile must be in parameters!")))
;

;;.
