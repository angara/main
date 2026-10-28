(ns notify-event-test
  (:require
    [clojure.test :refer [deftest is run-tests use-fixtures]]
    [clojure.string :as str]
    [sql.jdbc :as jdbc]
    [mount.core :as mount]
    [app.config :as config]
    [sql.conn :as conn]
    [sql.notify :as notify]
    [sql.notify-event :as events]))

(defn query [sql]
  (with-open [db (conn/dbc)] (jdbc/fetch db [sql])))

(defn execute [sql]
  (with-open [db (conn/dbc)] (jdbc/execute db [sql])))

(use-fixtures :each
  (fn [test-fn]
    (execute "truncate notify_event, notify_user restart identity")
    (test-fn)))

(defn enqueue []
  (:id (first (query "insert into notify_event(type,data) values ('forum_msg','{\"forum_msg\":{\"topic_id\":\"1\"}}') returning id"))))

(deftest success-and-job-atomicity
  (let [id (enqueue)
        row (events/claim)]
    (is (= id (:id row)))
    (is (nil? (events/claim)))
    (events/process! row
      (fn [event]
        (is (= id (:eid event)))
        (is (= "forum_msg" (:type event)))
        (notify/new-job 100 {:type "test"})))
    (is (= "done" (:status (first (query "select status from notify_event")))))
    (is (= 1 (:n (first (query "select count(*) as n from notify_user")))))
    ;; A stale worker must not dispatch a completed event twice.
    (events/process! row (fn [_] (throw (Exception. "must not run"))))))

(deftest rollback-retry-and-dead-letter
  (enqueue)
  (let [row (events/claim)
        failure (Exception. "simulated failure")]
    (try
      (events/process! row
        (fn [_]
          (notify/new-job 100 {:type "test"})
          (throw failure)))
      (catch Exception ex (events/retry! row ex)))
    (is (zero? (:n (first (query "select count(*) as n from notify_user")))))
    (is (= "wait" (:status (first (query "select status from notify_event")))))
    (is (nil? (events/claim)))
    (execute "update notify_event set available_at=now()-interval '1 second', attempts=9")
    (let [last-try (events/claim)]
      (events/retry! last-try failure)
      (is (= "failed" (:status (first (query "select status from notify_event"))))))))

(deftest recover-expired-claim
  (enqueue)
  (let [old (events/claim)]
    (execute "update notify_event set available_at=now()-interval '1 second', locked_until=now()-interval '1 second'")
    (let [new (events/claim)]
      (is (= 2 (:attempts new)))
      (events/process! old (fn [_] (throw (Exception. "stale attempt"))))
      (events/retry! old (Exception. "stale error"))
      (is (= "process" (:status (first (query "select status from notify_event")))))
      (events/process! new (fn [_] nil))
      (is (= "done" (:status (first (query "select status from notify_event"))))))))

(deftest concurrent-claim
  (let [ids (set [(enqueue) (enqueue)])
        gate (promise)
        workers (repeatedly 2 #(future @gate (events/claim)))
        workers (vec workers)]
    (deliver gate true)
    (is (= ids (set (map (comp :id deref) workers))))))

(defn -main []
  (mount/start-with-args
    {:psql {:jdbc-url (System/getenv "NOTIFY_TEST_JDBC_URL")}}
    #'config/conf #'conn/ds)
  (try
    (doseq [sql (str/split (slurp "sql/2026-10-28-notify-event.sql") #";")
            :when (not (str/blank? sql))]
      (execute sql))
    (execute (str "create table if not exists notify_user ("
                  "id varchar(24) primary key, ct bigint, inst bigint, status text, data jsonb)"))
    (let [result (run-tests 'notify-event-test)]
      (when (pos? (+ (:fail result) (:error result)))
        (throw (ex-info "notify_event tests failed" result))))
    (finally (mount/stop #'conn/ds #'config/conf))))
