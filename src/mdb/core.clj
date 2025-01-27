(ns mdb.core
  (:require
    [mount.core :refer [defstate]]
    [monger.json]
    [monger.joda-time]
    [monger.core :as mg]
    [app.config :refer [conf]])
  (:import
    [org.bson.types ObjectId]
    [com.mongodb WriteConcern]
    [org.joda.time DateTimeZone]
   ,))



(defmacro try-warn [label & body]
  `(try ~@body
    (catch Exception e#
      (~'warn ~label e#))))


(defn id_id [r]
  (if-let [id (:_id r)]
    (assoc (dissoc r :_id) :id id)
    r))



(defn connect [cnf]
  (-> (:tz conf "Asia/Irkutsk") DateTimeZone/forID DateTimeZone/setDefault)
  (let [mdb (mg/connect-via-uri (:url cnf))]
    (mg/set-default-write-concern! WriteConcern/FSYNC_SAFE)
    mdb))


(defn disconnect [mdb]
  (mg/disconnect (:conn mdb)))


; (defn indexes [mdb]
;   (let [db (:db mdb)])
;   ; (mc/ensure-index db user-coll {:auth 1})
;   ; (mc/ensure-index db story-coll (array-map :locs 1 :ct 1))
;   ; (mc/ensure-index db story-coll (array-map :ts 1))
;   mdb)
; ;

(defstate mdb
  :start
    (connect (-> conf :mdb-angara))
  :stop
    (disconnect mdb))


(defn dbc []
  (:db mdb))

;;; ;;; ;;; ;;; ;;;

(defn new_id [] (ObjectId.))

(defn oid [s]
  (try (ObjectId. s) 
    (catch Exception _ignore (str s))))

