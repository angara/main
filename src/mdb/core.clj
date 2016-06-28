
(ns mdb.core
  (:require
    [taoensso.timbre :refer [warn]]
    [clj-time.core :as t]
    [mount.core :refer [defstate]]
    [monger.json]
    [monger.joda-time]
    [monger.core :as mg]
    [monger.collection :as mc]
    [monger.query :as query]
    [mlib.conf :refer [conf]])
  (:import
    [org.bson.types ObjectId]
    [com.mongodb MongoOptions ServerAddress WriteConcern]
    [org.joda.time DateTimeZone]))
;


(defn id_id [r]
  (if-let [id (:_id r)]
    (assoc (dissoc r :_id) :id id)
    r))
;


(def seq-coll      "seq")

; (defn connect []
;   (let [cnf (:mdb conf)
;         ^ServerAddress sa (mg/server-address (:host cnf) (:port cnf))
;         ^MongoOptions opts (mg/mongo-options (:opts cnf))
;         conn (mg/connect sa opts)
;         db   (mg/get-db conn (:database cnf))
;         user (:user cnf)
;         pass (.toCharArray (:pass cnf ""))]
;
;       ; https://github.com/clojurewerkz/monger.docs/blob/master/articles/connecting.md
;       ; (require '[monger.credentials :as mcr])
;
;       ; (let [creds (mcr/for "username" "db-name" "pa$$w0rd")
;       ;       conn  (mg/connect-with-credentials "127.0.0.1" creds)]
;       ;       )
;
;     (mg/set-default-write-concern! WriteConcern/FSYNC_SAFE)
;     ; http://api.mongodb.org/java/current/com/mongodb/WriteConcern.html
;     ; WriteConcern/NORMAL, WriteConcern/REPLICAS_SAFE
;     db))
; ;

(defn connect [cnf]
  (-> (:tz conf "Asia/Irkutsk") DateTimeZone/forID DateTimeZone/setDefault)
  (let [mdb (mg/connect-via-uri (:uri cnf))]
    (mg/set-default-write-concern! WriteConcern/FSYNC_SAFE)
    mdb))
;

(defn disconnect [mdb]
  (mg/disconnect (:conn mdb)))
;

(defn indexes [mdb]
  (let [db (:db mdb)])
  ; (mc/ensure-index db user-coll {:auth 1})
  ; (mc/ensure-index db story-coll (array-map :locs 1 :ct 1))
  ; (mc/ensure-index db story-coll (array-map :ts 1))
  mdb)
;

(defstate mdb
  :start
    (-> conf :mdb connect indexes)
  :stop
    (disconnect mdb))
;

(defn dbc []
  (:db mdb))

(defn new_id [] (ObjectId.))

(defn oid [s]
  (try (ObjectId. s) (catch Exception e (str s))))


;;; ;;; ;;;

(defn next-serial [seq-name]
  (try
    (long (:n (mc/find-and-modify (dbc) seq-coll
                {:_id (name seq-name)}
                {"$inc" {:n (int 1)}}
                {:return-new true :upsert true})))
    (catch Exception e (warn "db/next-serial:" e))))
;


(defn next-sn [sn]
  (try
    (str (:n (mc/find-and-modify (dbc) seq-coll
                {:_id (name sn)}
                {"$inc" {:n (int 1)}}
                {:return-new true :upsert true})))
    (catch Exception e (warn "db/next-sn:" e))))

;;.
