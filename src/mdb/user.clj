
(ns mdb.user
  (:require
    [taoensso.timbre :refer [warn]]
    [clojure.core.cache :as cache]
    [monger.collection :as mc]
    [mlib.conf :refer [conf]]
    [mlib.core :refer [urand-bytes hexbyte]]
    [mlib.time :refer [now-ms]]
    [mdb.core :refer [dbc id_id]]))
;

(def sess-coll     "sess")
(def user-coll     "user")


(def FLDS_REQ_USER [:_id :login :name :family :upic :admin])

;;; sess

(defn new-sid []
  (apply str (format "%x" (now-ms)) "." (map hexbyte (urand-bytes 8))))
;


(def sess-cache
  (atom (cache/ttl-cache-factory {} :ttl 10000)))

(defn sess-load-mdb [sid]
  (try
    (mc/find-one-as-map (dbc) sess-coll {:_id sid})
    (catch Exception e (warn "sess-load-mdb:" e))))
;

(defn sess-load [sid]
  (when sid
    (if-let [s (cache/lookup @sess-cache sid)]
      (do
        (swap! sess-cache #(cache/hit % sid))
        s)
      (let [s (sess-load-mdb sid)]
        (if s
          (swap! sess-cache #(cache/miss % sid s))
          (swap! sess-cache #(cache/evict % sid)))
        s))))
;

(defn sess-update [sid data]
  (swap! sess-cache #(cache/evict % sid))
  (try
    (= 1 (.getN (mc/update-by-id (dbc) sess-coll sid {:$set data})))
    (catch Exception e (warn "sess-update:" e))))
;

(defn sess-new [data]
  (let [data (assoc data :_id (new-sid))]
    (try
      (mc/insert-and-return (dbc) sess-coll data)
      (catch Exception e (warn "sess-new:" e)))))
;

;;; user

(defn user-by-id [uid flds]
  (when uid
    (try
      (id_id (mc/find-map-by-id (dbc) user-coll uid flds))
      (catch Exception e (warn "user-by-id:" e)))))
;

(defn users-by-ids [uids flds]
  (when-let [uv (not-empty (vec uids))]
    (try
      (map id_id (mc/find-maps (dbc) user-coll {:_id {:$in uv}} flds))
      (catch Exception e (warn "users-by-ids:" e)))))
;

(defn user-by-auth [auth flds]
  (when-let [a (str auth)]
    (try
      (id_id (mc/find-one-as-map (dbc) user-coll {:auth a} flds))
      (catch Exception e (warn "db/user-by-auth:" e)))))
;

(defn user-create [data]
  (try
    (mc/insert-and-return (dbc) user-coll data)
    (catch Exception e (warn "db/user-create:" e))))
;

(defn user-update [uid fset]
  (try
    (= 1 (.getN (mc/update-by-id (dbc) user-coll uid {:$set fset})))
    (catch Exception e (warn "db/user-update:" e))))

;;.

;  _id
;  auth ["fb:123456...", ... ]
;  fb {
;    "id":"10152555555555555"
;    "first_name":"fname"
;    "last_name":"lastlast"
;    "name":"first last"
;    "gender":"male"
;    "link":"https://www.facebook.com/app_scoped_user_id/101525555555555555555/"
;    "locale":"en_US"
;    "timezone":8
;    "verified":true
;  }
