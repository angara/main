
(ns mlib.conf
  (:require
    [mount.core :refer [defstate args]]
    [mlib.core :refer [deep-merge]]))
;

(defstate conf
  :start
    (apply deep-merge (args)))
;

;.
