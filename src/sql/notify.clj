(ns sql.notify
  "forum/mail queries for notifications (merged from angara/notify,
   ported from next.jdbc to the app clojure.jdbc stack)."
  (:require
    [honeysql.helpers :as h]
    ;
    [jdbc.core :as jdbc]
    [mlib.core :refer [to-long]]
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
