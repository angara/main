
(ns db.sql
  (:require
    [clojure.java.jdbc :as jdbc]
    [taoensso.timbre :refer [info warn]]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]
    [mlib.db.psql :refer [make-pool]]))
;

(defstate conn
  :start
    (make-pool (:psql conf))
  :stop
    (.close (:datasource conn)))
;

;;.
