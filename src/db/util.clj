
(ns db.util
  (:require
    [taoensso.timbre :refer [warn]]))
;

(defmacro try-warn [label & body]
  `(try ~@body
    (catch Exception e#
      (warn ~label (.getMessage e#)))))
;

;;.
