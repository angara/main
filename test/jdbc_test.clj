(ns jdbc-test
  (:require
    [clojure.test :refer [deftest is run-tests]]
    [mount.core :as mount]
    [next.jdbc :as next-jdbc]
    [app.config :as config]
    [sql.conn :as conn]
    [sql.core :as sql]
    [sql.jdbc :as jdbc]
    [sql.notify :as notify]
    [sql.user :as user]
    [sql.sess :as sess]
    [sql.calendar :as calendar]
    [web.app :as web]
    [forum.topic :as topic]
    [forum.db :as forum-db]
    [angara.webapp.db.psql :as webapp]
    [angara.webapp.app.middleware :as middleware]
    [notify-event-test])
  (:import
    (java.time Instant LocalDate LocalDateTime LocalTime)
    (java.util TimeZone)))

(deftest result-contracts
  (with-open [db (conn/dbc)]
    (is (instance? java.sql.Connection db))
    (is (= [] (jdbc/fetch db ["select 1 where false"])))
    (is (nil? (jdbc/fetch-one db ["select 1 where false"])))
    (is (= {:mixedcase 1 :nilvalue nil}
           (jdbc/fetch-one db ["select 1 as \"MixedCase\", null as nilvalue"])))
    (jdbc/execute db ["create temporary table jdbc_contract(id serial primary key, value text)"])
    (is (= 1 (jdbc/execute db ["insert into jdbc_contract(value) values (?)" "first"])))
    (is (= [{:id 2}] (jdbc/execute db ["insert into jdbc_contract(value) values (?) returning id" "second"])))
    (is (= 2 (jdbc/execute db ["update jdbc_contract set value=?" "updated"])))
    (is (= 0 (jdbc/execute db ["delete from jdbc_contract where id=99"])))
    (is (= 2 (jdbc/execute db ["delete from jdbc_contract"])))
    (let [row (jdbc/fetch-one db ["select '{}'::jsonb as data, array[1,2] as items"])]
      (is (instance? org.postgresql.util.PGobject (:data row)))
      (is (= [1 2] (vec (.getArray ^java.sql.Array (:items row))))))))

(deftest date-contracts
  (with-open [db (conn/dbc)]
    (doseq [zone ["UTC" "Asia/Irkutsk"]]
      (let [old (TimeZone/getDefault)]
        (try
          (TimeZone/setDefault (TimeZone/getTimeZone zone))
          (let [local (LocalDateTime/parse "2026-10-28T12:34:56")
                instant (Instant/parse "2026-10-28T04:34:56Z")
                date (LocalDate/parse "2026-10-28")
                time (LocalTime/parse "12:34:56")
                row (jdbc/fetch-one db
                      ["select ?::timestamp as local, ?::timestamptz as instant, ?::date as date, ?::time as time"
                       local instant date time])]
            (is (= local (:local row)))
            (is (= (LocalDateTime/ofInstant instant (.toZoneId (TimeZone/getDefault)))
                   (:instant row)))
            (is (= date (:date row)))
            (is (= time (:time row))))
          (finally (TimeZone/setDefault old)))))))

(deftest core-and-returning-contracts
  (is (true? (sql/insert-into :jdbc_test_rows {:value "test"})))
  (is (= 1 (sql/exec {:update :jdbc_test_rows :set {:value "updated"}})))
  (is (= "updated" (:value (sql/fetch-one {:select [:*] :from [:jdbc_test_rows]}))))
  (is (= [] (sql/fetch {:select [:*] :from [:jdbc_test_rows] :where [:= :id -1]})))
  (is (nil? (sql/fetch-one {:select [:*] :from [:jdbc_test_rows] :where [:= :id -1]})))
  (is (integer? (sql/next-id "jdbc_test_rows")))
  (is (= 42 (notify/state-var-put "jdbc-test" 42)))
  (is (= "value" (notify/state-var-puts "jdbc-test" "value")))
  (is (= 42 (notify/state-var-get "jdbc-test")))
  (is (= "value" (notify/state-var-gets "jdbc-test"))))

(deftest skip-locked-contract
  (with-open [first-db (conn/dbc)
              second-db (conn/dbc)]
    (next-jdbc/with-transaction [tx first-db]
      (jdbc/fetch tx ["select * from jdbc_test_rows for update"])
      (is (= [] (jdbc/fetch second-db ["select * from jdbc_test_rows for update skip locked"]))))))

(deftest webapp-contracts
  (is (= [{:value 1}] (webapp/exec! ["select 1 as value"])))
  (is (= {:value 1} (webapp/exec-one! ["select 1 as value"])))
  (is (nil? (webapp/exec-one! ["select 1 where false"])))
  (is (instance? LocalDateTime
        (:ts (webapp/exec-one! ["select current_timestamp as ts"])))))

(deftest strict-errors
  (is (thrown? java.sql.SQLException
        (sql/exec {:insert-into :jdbc_missing_table :values [{:id 1}]})))
  (is (thrown? java.sql.SQLException
        (sql/fetch {:select [:*] :from [:jdbc_missing_table]})))
  (is (thrown? java.sql.SQLException
        (sql/fetch-one {:select [:*] :from [:jdbc_missing_table]})))
  (is (thrown? java.sql.SQLException (sql/next-id :jdbc_missing_table))))

(deftest caller-transaction
  (with-open [db (conn/dbc)]
    (let [before (:n (jdbc/fetch-one db ["select count(*) as n from jdbc_test_rows"]))]
      (is (thrown? java.sql.SQLException
            (next-jdbc/with-transaction [tx db]
              (sql/exec-on! tx {:insert-into :jdbc_test_rows :values [{:value "rollback"}]})
              (is (= "rollback" (:value (sql/fetch-one-on tx
                         {:select [:value] :from [:jdbc_test_rows] :where [:= :value :?v]}
                         {:v "rollback"}))))
              (is (= 1 (count (sql/fetch-on tx
                         {:select [:*] :from [:jdbc_test_rows] :where [:= :value "rollback"]}))))
              (sql/exec-on! tx {:insert-into :jdbc_missing_table :values [{:id 1}]}))))
      (is (= before (:n (jdbc/fetch-one db ["select count(*) as n from jdbc_test_rows"])))))
    (is (= {:ok 1} (jdbc/fetch-one db ["select 1 as ok"])))))

(deftest user-auth-atomicity
  (with-open [db (conn/dbc)]
    (jdbc/execute db ["delete from user_auth"])
    (jdbc/execute db ["delete from users"])
    (let [data {:username "test" :email "test@example.invalid" :auth ["un:test" "invalid"]}]
      (is (thrown? java.sql.SQLException (user/user-create "100" data)))
      (is (zero? (:n (jdbc/fetch-one db ["select count(*) as n from users"])))))
    (is (zero? (:n (jdbc/fetch-one db ["select count(*) as n from user_auth"]))))
    (is (= "100" (:id (user/user-create "100"
                         {:username "test" :email "test@example.invalid" :auth ["un:test"]}))))
    (is (= 1 (:n (jdbc/fetch-one db ["select count(*) as n from user_auth"]))))
    (is (thrown? java.sql.SQLException
          (user/user-create "100" {:username "duplicate"})))
    (is (thrown? java.sql.SQLException (user/user-update "100" {:missing_column 1})))))

(deftest application-writes-propagate
  ;; Inject a DB failure without touching real session/calendar tables.
  (with-redefs [sql/exec (fn [& _] (throw (java.sql.SQLException. "failure")))]
    (is (thrown? java.sql.SQLException (sess/sess-new {:tmp true})))
    (is (thrown? java.sql.SQLException (sess/sess-update "test" {:tmp true})))
    (is (thrown? java.sql.SQLException (sess/clear-sess "test")))
    (is (thrown? java.sql.SQLException (sess/clear-uid-sess "100")))
    (is (thrown? java.sql.SQLException (calendar/add-crec {:title "test"})))))

(deftest safe-http-error
  (let [fail (fn [_] (throw (java.sql.SQLException. "secret submitted value" "23505")))]
    (doseq [inner [fail
                  (middleware/wrap-resp-exceptions
                    (middleware/wrap-error-page fail))]]
      (let [resp ((web/wrap-sql-errors inner) {})]
        (is (= 503 (:status resp)))
        (is (not (.contains ^String (:body resp) "secret")))))))

(deftest forum-sql-errors-reach-boundary
  (let [fail (fn [& _] (throw (java.sql.SQLException. "secret" "08006")))
        req {:user {:id "100"} :params {:tid "1" :tids ["1"] :watch "1"}}]
    (with-redefs [sql/fetch fail sql/exec fail forum-db/lastreads fail]
      (doseq [handler [topic/get-lastreads topic/get-watch topic/set-watch]]
        (let [resp ((web/wrap-sql-errors handler) req)]
          (is (= 503 (:status resp)))
          (is (not (.contains ^String (:body resp) "secret"))))))))

(defn -main []
  ;; Initialize and run the existing queue tests on the same isolated database.
  (notify-event-test/-main)
  (mount/start-with-args
    {:psql {:jdbc-url (System/getenv "NOTIFY_TEST_JDBC_URL")}}
    #'config/conf #'conn/ds)
  (try
    (with-open [db (conn/dbc)]
      (doseq [stmt ["drop table if exists jdbc_test_rows"
                    "create table jdbc_test_rows(id serial primary key, value text)"
                    "create table if not exists state_vars(vname text primary key, ts timestamptz, i bigint, s text)"
                    "create table if not exists users(uid integer primary key, login text, email text, name text, surname text, town text, note text, active boolean, created timestamp, atime timestamp, ntime timestamp, passhash text, passcode text, upic text, ts timestamp)"
                    "create table if not exists user_auth(auth_type text not null, auth_id text not null, user_id text not null, primary key(auth_type,auth_id))"]]
        (jdbc/execute db [stmt])))
    (let [result (run-tests 'jdbc-test)]
      (when (pos? (+ (:fail result) (:error result)))
        (throw (ex-info "JDBC compatibility tests failed" result))))
    (finally (mount/stop #'conn/ds #'config/conf))))
