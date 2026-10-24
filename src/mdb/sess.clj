(ns mdb.sess
  (:require
    [taoensso.telemere :refer [log!]]
    [clojure.core.cache :as cache]
    [clj-time.core :as tc]
    [mount.core :refer [defstate]]
    [monger.collection :as mc]
    [mlib.core :refer [urand-bytes hexbyte]]
    [mlib.time :refer [now-ms]]
    [mdb.core :refer [dbc]]))


(def SESS "sess")

(comment
  :fields
  {:_id  "sid"
   :uid  "str uid"
   :ct   "ctime"
   :ts   "ts / atime"
   :ip   "ip"
   :tmp  "temporary?"})


(defn create-indexes [db]
  (try
    (mc/create-index db SESS (array-map :uid 1))
    (catch Exception ex
      (log! :warn ["create-indexes:" ex]))))


(def ATIME_INTERVAL (tc/seconds 10))

(def SESS_CACHE_TTL 10000)  ;; 10 sec

(def *sess-cache
  (atom (cache/ttl-cache-factory {} :ttl SESS_CACHE_TTL)))


;;; ;;; ;;; ;;;

(defn new-sid []
  (apply str (format "%x" (now-ms)) "." (map hexbyte (urand-bytes 10))))


(defn new-csrf []
  (apply str (map hexbyte (urand-bytes 10))))


(defn sess-new [data]
  (let [now (tc/now)]
    (try
      (mc/insert-and-return (dbc) SESS
                            (assoc data
                                   :_id (new-sid)
                                   :ct now
                                   :ts now
                                   :_csrf (new-csrf)))
      (catch Exception ex
        (log! :warn ["db/sess-create:" ex])))))


(defn sess-update [sid data]
  (swap! *sess-cache #(cache/evict % sid))
  (try
    (->
     (mc/update-by-id (dbc) SESS sid {:$set data})
     (.getN) (= 1))
    (catch Exception ex
      (log! :warn ["db/sess-update:" ex]))))


(defn sess-load-mdb [sid]
  (try
    (mc/find-one-as-map (dbc) SESS {:_id sid})
    (catch Exception ex
      (log! :warn ["sess-load-mdb:" ex]))))


(defn sess-load [sid]
  (when sid
    (let [sess
          (if-let [s (cache/lookup @*sess-cache sid)]
            (do
              (swap! *sess-cache #(cache/hit % sid))
              s)
            (let [s (sess-load-mdb sid)]
              (if s
                (swap! *sess-cache #(cache/miss % sid s))
                (swap! *sess-cache #(cache/evict % sid)))
              s))]
      (when (tc/before?
             (:ts sess (tc/date-time 1970 1 1))
             (tc/ago ATIME_INTERVAL))
        (sess-update sid {:ts (tc/now)}))
      sess)))


(defn clear-sess [sid]
  (try
    (swap! *sess-cache #(cache/evict % sid))
    (->
     (mc/remove (dbc) SESS {:_id sid})
     (.getN)
     (= 1))
    (catch Exception ex
      (log! :warn ["db/clear-sess:" sid ex]))))


(defn clear-uid-sess [uid]
  ;; cached sessions not handled!
  (try
    (.getN (mc/remove (dbc) SESS {:uid uid}))
    (catch Exception ex
      (log! :warn ["db/clear-uid-sess:" uid ex]))))


(defstate indexes
  :start (create-indexes (dbc)))
