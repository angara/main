;;;;
;;  angara-main
;;;;

(ns web.main
  (:require
    [clojure.edn :as edn]
    [mount.core :refer [start-with-args]]
    [mlib.log :refer [warn]]
    [mlib.conf :refer [conf]]
    [photomap.core]
    [web.srv])
  (:gen-class))
;

(defn -main [& args]
  (if-let [rc (-> args first slurp edn/read-string)]
    (start-with-args rc)
    (warn "config profile must be in parameters!")))
;

;;.
