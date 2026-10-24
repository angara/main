(ns mdb.core
  (:require
    [mount.core :refer [defstate]]
    [monger.json]
    [monger.core :as mg]
    [monger.collection :as mc]
    [monger.conversion :refer [ConvertToDBObject ConvertFromDBObject to-db-object]]
    [taoensso.telemere :refer [log!]]
    [app.config :refer [conf]])
  (:import
    [org.bson.types ObjectId]
    [com.mongodb WriteConcern]
    (java.time LocalDateTime ZonedDateTime ZoneId)
    (java.util Date TimeZone)
   ,))



(defmacro try-warn [label & body]
  `(try ~@body
    (catch Exception e#
      (~'warn ~label e#))))


(defn id_id [r]
  (if-let [id (:_id r)]
    (assoc (dissoc r :_id) :id id)
    r))



;; java.time <-> BSON Date conversion (replaces monger.joda-time).
;; Read direction is global: every Date field comes back as LocalDateTime
;; in the default zone (== conf :tz == Asia/Irkutsk).

(extend-protocol ConvertToDBObject
  LocalDateTime
  (to-db-object [^LocalDateTime input]
    (to-db-object (Date/from (.toInstant (.atZone input (ZoneId/systemDefault))))))
  ZonedDateTime
  (to-db-object [^ZonedDateTime input]
    (to-db-object (Date/from (.toInstant input)))))

(extend-protocol ConvertFromDBObject
  java.util.Date
  (from-db-object [^Date input _keywordize]
    (LocalDateTime/ofInstant (.toInstant input) (ZoneId/systemDefault))))



(defn connect [cnf]
  (TimeZone/setDefault (TimeZone/getTimeZone (or (:tz conf) "Asia/Irkutsk")))
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


(def SEQ "seq")


(defn next-sn
  "next serial number from mongodb sequence collection"
  [sn]
  (try
    (->
     (mc/find-and-modify (dbc) SEQ
                         {:_id (name sn)}
                         {"$inc" {:n (int 1)}}
                         {:return-new true :upsert true})
     (:n) (str))
    (catch Exception ex
      (log! :warn ["db/next-sn:" ex]))))

;;; ;;; ;;; ;;; ;;;

(defn new_id [] (ObjectId.))

(defn oid [s]
  (try (ObjectId. s) 
    (catch Exception _ignore (str s))))

