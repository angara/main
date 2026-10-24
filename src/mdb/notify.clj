(ns mdb.notify
  "notify_user job queue in mongodb (merged from angara/notify)."
  (:require
    [tick.core :as t]
    [mount.core :refer [defstate]]
    [monger.collection :as mc]
    [monger.query :as mq]
    ;
    [mlib.core :refer [now-ms]]
    [mdb.core :refer [dbc id_id]]
    ,)
  (:import
    (org.bson.types ObjectId)))


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
  (let [atime (t/<< (t/date-time) active-duration)]
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
