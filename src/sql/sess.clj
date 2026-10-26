(ns sql.sess
  "web sessions in postgres (was mongodb collection, 2026-10-26).
   Rows are shaped like the old mongo documents: :_id = sid,
   :uid as string, :_csrf; java (AuthFilter) updates ts/ip columns."
  (:require
    [taoensso.telemere :refer [log!]]
    ;
    [clojure.core.cache :as cache]
    [tick.core :as t]
    [java-time.api :as jt]
    [honeysql.helpers :as h]
    ;
    [mlib.core :refer [to-int urand-bytes hexbyte]]
    [sql.core :refer [fetch-one exec]]
   ,))


(def ATIME_INTERVAL (t/of-seconds 10))

(def SESS_CACHE_TTL 10000)  ;; 10 sec

(def *sess-cache
  (atom (cache/ttl-cache-factory {} :ttl SESS_CACHE_TTL)))


;;; ;;; ;;; ;;;

(defn new-sid []
  (apply str (format "%x" (System/currentTimeMillis)) "."
    (map hexbyte (urand-bytes 10))))

(defn new-csrf []
  (apply str (map hexbyte (urand-bytes 10))))


(defn- row->sess [r]
  (when r
    { :_id   (:sid r)
      :uid   (some-> (:uid r) str)
      :ct    (:ct r)
      :ts    (:ts r)
      :ip    (:ip r)
      :tmp   (:tmp r)
      :_csrf (:csrf r)
      ,}))


(def SESS_COLS_MAP
  {:_csrf :csrf})


(defn sess-new [data]
  (let [sid (new-sid)
        csrf (new-csrf)]
    (try
      (exec
        { :insert-into :sess
          :values
          [{ :sid  sid
             :uid  (to-int (:uid data))
             :ct   (t/date-time)
             :ts   (t/date-time)
             :ip   (:ip data)
             :tmp  (boolean (:tmp data))
             :csrf csrf
             ,}]})
      (assoc data :_id sid :_csrf csrf)
      (catch Exception ex
        (log! :warn ["sess-new:" ex])))))


(defn sess-update [sid data]
  (swap! *sess-cache #(cache/evict % sid))
  (try
    (= 1
      (exec
        (->
          (h/update :sess)
          (h/sset
            (into {}
              (map (fn [[k v]] [(or (SESS_COLS_MAP k) k) v]))
              data))
          (h/where [:= :sid (str sid)]))))
    (catch Exception ex
      (log! :warn ["sess-update:" sid ex]))))


(defn- sess-load-pg [sid]
  (try
    (row->sess
      (fetch-one
        (->
          (h/select :*)
          (h/from :sess)
          (h/where [:= :sid (str sid)]))))
    (catch Exception ex
      (log! :warn ["sess-load-pg:" sid ex]))))


(defn sess-load [sid]
  (when sid
    (let [sess
          (if-let [s (cache/lookup @*sess-cache sid)]
            (do
              (swap! *sess-cache #(cache/hit % sid))
              s)
            (let [s (sess-load-pg sid)]
              (if s
                (swap! *sess-cache #(cache/miss % sid s))
                (swap! *sess-cache #(cache/evict % sid)))
              s))]
      (when (t/<
             (:ts sess (jt/local-date-time 1970 1 1))
             (t/<< (t/date-time) ATIME_INTERVAL))
        (sess-update sid {:ts (t/date-time)}))
      sess)))


(defn clear-sess [sid]
  (try
    (swap! *sess-cache #(cache/evict % sid))
    (= 1
      (exec
        (->
          (h/delete-from :sess)
          (h/where [:= :sid (str sid)]))))
    (catch Exception ex
      (log! :warn ["clear-sess:" sid ex]))))


(defn clear-uid-sess [uid]
  ;; cached sessions not handled!
  (try
    (exec
      (->
        (h/delete-from :sess)
        (h/where [:= :uid (to-int uid)])))
    (catch Exception ex
      (log! :warn ["clear-uid-sess:" uid ex]))))
