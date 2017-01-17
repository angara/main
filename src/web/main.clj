;;;;
;;  angara-main
;;;;

(ns web.main
  (:gen-class)
  (:require
    [clojure.edn :as edn]
    [taoensso.timbre :refer [info error merge-config!]]
    [mount.core :refer [defstate start-with-args]]
    [mlib.conf :refer [conf]]
    [web.srv]
    [bots.meteo.core]
    [photomap.core]))
;

(merge-config!
  {:timestamp-opts
    {:pattern  "yy-MM-dd HH:mm:ss"
     :locale   :jvm-default
     :timezone (java.util.TimeZone/getDefault)}})
;

(defn -main [& args]
  (if-let [rc (-> args first slurp edn/read-string)]
    (start-with-args rc)
    (error "config profile must be in parameters!")))
;

;;.
