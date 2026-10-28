(ns sql.notify-event
  "Durable inbound queue. The attempt number fences expired workers."
  (:require
    [next.jdbc :as next-jdbc]
    [sql.jdbc :as jdbc]
    [jsonista.core :as json]
    [sql.conn :refer [dbc]]
    [sql.notify :as notify]))

(defn claim []
  (with-open [conn (dbc)]
    (jdbc/fetch-one conn
      [(str "update notify_event set status='process', attempts=attempts+1,"
            " locked_until=current_timestamp + interval '5 minutes',"
            " available_at=current_timestamp + interval '5 minutes'"
            " where id=(select id from notify_event"
            " where status in ('wait','process') and available_at<=current_timestamp"
            " order by available_at,id limit 1 for update skip locked)"
            " returning id,type,data,attempts")])))

(defn event-data [{:keys [id type data]}]
  (assoc (json/read-value (.getValue ^org.postgresql.util.PGobject data)
                         json/keyword-keys-object-mapper)
         :eid id :type type))

(defn process! [{:keys [id attempts] :as row} dispatch]
  (with-open [conn (dbc)]
    (next-jdbc/with-transaction [tx conn]
      ;; Lock before dispatch: another worker cannot reclaim during processing.
      (when (jdbc/fetch-one tx
              ["select id from notify_event where id=? and status='process' and attempts=? for update"
               id attempts])
        (binding [notify/*job-conn* tx]
          (dispatch (event-data row)))
        (jdbc/execute tx
          ["update notify_event set status='done', data='{}'::jsonb, processed_at=current_timestamp, locked_until=null, last_error=null where id=?"
           id])))))

(defn retry! [{:keys [id attempts]} ex]
  (with-open [conn (dbc)]
    (jdbc/execute conn
      [(str "update notify_event set status=case when attempts>=10 then 'failed' else 'wait' end,"
            " available_at=current_timestamp + (? * interval '1 second'),"
            " locked_until=null, last_error=?"
            " where id=? and status='process' and attempts=?")
       (long (min 3600 (* 5 (bit-shift-left 1 (min attempts 10)))))
       (subs (str (.getMessage ^Exception ex)) 0
             (min 2000 (count (str (.getMessage ^Exception ex)))))
       id attempts])))
