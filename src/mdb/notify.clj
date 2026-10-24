(ns mdb.notify
  "notify_user job queue in mongodb (merged from angara/notify)."
  (:require
    [java-time.api :as jt]
    [mount.core :refer [defstate]]
    [monger.collection :as mc]
    [monger.query :as mq]
    [monger.conversion :refer [ConvertToDBObject to-db-object]]
    ;
    [mlib.core :refer [now-ms]]
    [mdb.core :refer [dbc id_id]]
    ,)
  (:import
    (java.time LocalDateTime ZonedDateTime ZoneId)
    (java.util Date)
    (org.bson.types ObjectId)))


;; query params may be java.time; convert to Date for monger.
;; read direction is NOT extended on purpose: monger.joda-time owns
;; ConvertFromDBObject (java.util.Date -> joda) for the rest of the app.

(extend-protocol ConvertToDBObject
  LocalDateTime
  (to-db-object [^LocalDateTime input]
    (to-db-object (Date/from (.toInstant (.atZone input (ZoneId/systemDefault))))))
  ZonedDateTime
  (to-db-object [^ZonedDateTime input]
    (to-db-object (Date/from (.toInstant input)))))


(def USER "user")
(def NOTIFY_USER_QUEUE "notify_user")


(def WAIT    "wait")
(def PROCESS "process")


(comment
  :notify_user
  {
    :_id "str-oid"
    :inst "<int> action timestamp"
    :state #{WAIT PROCESS}    ;; ?DONE ?FAIL
    ;
    ;; :user_id "uid"
    ;; :type  #{private_message forum_message forum_topic}
    ;; :other values;
    }
  ,)


(defn create-indexes [db]
  (mc/create-index db NOTIFY_USER_QUEUE (array-map :inst 1))
  db)


(defstate indexes
  :start (create-indexes (dbc)))


;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn get-user [id]
  (mc/find-map-by-id (dbc) USER id))


(defn user-online? [id active-duration]
  (let [atime (jt/minus (jt/local-date-time) active-duration)]
    (mc/find-one-as-map (dbc) USER
      { :_id id
        :atime {:$gte atime}}
      [:_id :atime])))


;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn new-job [time-ms job-data]
  (mc/insert-and-return (dbc) NOTIFY_USER_QUEUE
    (assoc job-data
      :_id (str (ObjectId.))
      :ct (now-ms)
      :inst time-ms
      :status WAIT)))


(defn peek-job [query]
  (-> (dbc)
    (mq/with-collection (name NOTIFY_USER_QUEUE)
      (mq/find (assoc query :status WAIT))
      (mq/sort {:inst 1})
      (mq/limit 1))
    (first)
    (id_id)))


(defn select-job [query]
  (id_id
    (mc/find-and-modify (dbc) NOTIFY_USER_QUEUE
    (assoc query :status WAIT)
    {:$set {:status PROCESS :ts (now-ms)}}
    {:sort {:inst 1} :return-new true})))


(defn finish-job [id]
  (mc/remove-by-id (dbc) NOTIFY_USER_QUEUE id))
