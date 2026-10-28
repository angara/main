(ns sql.notify
  "forum/mail queries and the notify_user job queue (merged from
   angara/notify, ported from next.jdbc to the app clojure.jdbc stack).
   Job queue migrated from mongodb to pg table notify_user (2026-10-26)."
  (:require
    [honeysql.helpers :as h]
    ;
    [jsonista.core :as json]
    [jdbc.core :as jdbc]
    [mlib.core :refer [to-long now-ms hexbyte urand-bytes]]
    [sql.conn :refer [dbc]]
    [sql.core :refer [fetch fetch-one]]
    ,))


;; ;; ;; ;; ;; ;; ;; ;; ;;

(def FORUM_TOPICS   :forum_topics)
(def FORUM_LASTREAD :forum_lastread)

(def PRIVATE_MESSAGES :mail_msg)


(defn get-mail [id]
  (when-let [id (to-long id)]
    (->
      (h/select :*)
      (h/from PRIVATE_MESSAGES)
      (h/where [:= :id id])
      (fetch-one))))


(defn mail-read? [mail]
  (:dest_time mail))


;; ;; ;; ;; ;; ;; ;; ;; ;;

(def SELECT_WATCHED_UNREAD
  (->
    (h/select :lr.uid :lr.tid :lr.msgid)
    (h/from [FORUM_LASTREAD :lr])
    (h/join [FORUM_TOPICS :tp]
      [:= :tp.tid :lr.tid])
    (h/where
      [:= :tp.tid :?tid]
      [:> :lr.watch 0]
      [:> :tp.lastmsgid :lr.msgid])))


(defn topic-unread-watch [topic-id]
  (when-let [tid (to-long topic-id)]
    (fetch
      SELECT_WATCHED_UNREAD
      {:tid tid})))


;; ;; ;; ;; ;; ;; ;; ;; ;;

(def SELECT_USER_UNREAD_TOPIC
  (->
    (h/select :tp.tid :tp.title :tp.lastupdate)
    (h/from [FORUM_LASTREAD :lr])
    (h/join [FORUM_TOPICS :tp]
      [:= :tp.tid :lr.tid])
    (h/where
      [:= :lr.tid :?tid]
      [:= :lr.uid :?uid]
      [:> :lr.watch 0]
      [:> :tp.lastmsgid :lr.msgid])))


(defn user-unread-topic [user-id topic-id]
  (when-let [[tid uid] (and user-id topic-id
                            [(to-long topic-id) (to-long user-id)])]
    (fetch-one
      SELECT_USER_UNREAD_TOPIC
      { :tid tid
        :uid uid})))


;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn exec-raw
  "execute raw [sql & params] vector"
  [sql-vec]
  (with-open [conn (dbc)]
    (jdbc/execute conn sql-vec)))


;;    state_vars
;;
;;  create table state_vars(vname varchar(80) primary key, ts timestamptz, i int8, s varchar);

(defn state-var-get [^String var-name]
  (->
    (h/select :i)
    (h/from :state_vars)
    (h/where [:= :vname :?var-name])
    (fetch-one {:var-name var-name})
    (:i)))


(defn state-var-put [^String var-name i]
  (-> [(str "insert into state_vars(vname,ts,i) values (?,CURRENT_TIMESTAMP,?)"
            "on conflict (vname) do update set ts=EXCLUDED.ts, i=EXCLUDED.i returning *")
       var-name i]
      (exec-raw)
      (first)
      (:i)))


(defn state-var-gets [^String var-name]
  (->
    (h/select :s)
    (h/from :state_vars)
    (h/where [:= :vname :?var-name])
    (fetch-one {:var-name var-name})
    (:s)))


(defn state-var-puts [^String var-name ^String s]
  (-> [(str "insert into state_vars(vname,ts,s) values (?,CURRENT_TIMESTAMP,?)"
            "on conflict (vname) do update set ts=EXCLUDED.ts, s=EXCLUDED.s returning *")
       var-name s]
      (exec-raw)
      (first)
      (:s)))


;; ;; ;; ;; ;; ;; ;; ;; ;;
;;    notify_user job queue (was mongodb collection, 2026-10-26)

(def WAIT    "wait")
(def PROCESS "process")


(defn- new-job-id []
  ;; 24-hex string, same shape the mongo ObjectId had
  (apply str (map hexbyte (urand-bytes 12))))


(defn- jsonb->clj [v]
  (when v
    (json/read-value
      (if (instance? org.postgresql.util.PGobject v)
        (.getValue ^org.postgresql.util.PGobject v)
        (str v))
      json/keyword-keys-object-mapper)))


(defn- row->job [r]
  (when r
    (assoc (jsonb->clj (:data r))
           :id (:id r)
           :inst (:inst r))))


(def ^:dynamic *job-conn* nil)

(defn new-job [time-ms job-data]
  (let [id (new-job-id)]
    (let [query ["insert into notify_user (id, ct, inst, status, data) values (?,?,?,?,?::jsonb)"
                 id (now-ms) (long time-ms) WAIT (json/write-value-as-string job-data)]]
      (if *job-conn*
        (jdbc/execute *job-conn* query)
        (exec-raw query)))
    (assoc job-data :id id :inst time-ms)))


(defn peek-job []
  ;; earliest waiting job, without claiming it
  (->
    (with-open [conn (dbc)]
      (jdbc/fetch conn
        ["select id, inst from notify_user where status = ? order by inst asc limit 1"
         WAIT]))
    (first)))


(defn select-job []
  ;; atomically claim the earliest waiting job
  (->
    (with-open [conn (dbc)]
      (jdbc/fetch conn
        [(str "update notify_user set status = ?, ts = ?"
              "  where id = (select id from notify_user where status = ?"
              "              order by inst asc limit 1 for update skip locked)"
              "    and status = ?"
              "  returning id, inst, data")
         PROCESS (now-ms) WAIT WAIT]))
    (first)
    (row->job)))


(defn finish-job [id]
  (exec-raw ["delete from notify_user where id = ?" (str id)]))
