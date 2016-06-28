
(ns bots.meteo.data
  (:require
    [taoensso.timbre :refer [warn]]
    [monger.collection :as mc]
    [meteo.db :refer [db st-near] :rename {db mconn}]))


; (defn st-near [ll]
;   (try
;     (mc/find-maps (mconn) :st {:pub _id sid})
;     (catch Exception e (warn "st-near:" e))))
