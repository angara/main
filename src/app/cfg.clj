(ns app.cfg
  (:require
    [mount.core :refer [defstate]]
    [mlib.config :refer [conf]]))
;=

(defstate cfg
  :start
    (:main conf))
;=

;;.
