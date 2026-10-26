(ns mdb.notify
  "user collection access for the notify worker (mongodb).
   The notify_user job queue moved to postgres - see sql.notify (2026-10-26)."
  (:require
    [tick.core :as t]
    [monger.collection :as mc]
    ;
    [mdb.core :refer [dbc]]
    ,))


(def USER "user")


(defn get-user [id]
  (mc/find-map-by-id (dbc) USER id))


(defn user-online? [id active-duration]
  (let [atime (t/<< (t/date-time) active-duration)]
    (mc/find-one-as-map (dbc) USER
      { :_id id
        :atime {:$gte atime}}
      [:_id :atime])))
