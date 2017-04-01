;;;;
;;  angara-main
;;;;

(ns web.main
  (:gen-class)
  (:require
    [clojure.edn :as edn]
    [mount.core :refer [defstate start-with-args]]
    [mlib.log :refer [warn]]
    [mlib.conf :refer [conf]]
    [web.srv]
    [bots.meteo.core]
    [photomap.core]))
;

(defn -main [& args]
  (if-let [rc (-> args first slurp edn/read-string)]
    (start-with-args rc)
    (warn "config profile must be in parameters!")))
;

;;.
