(ns sql.conn
  (:require
    [clj-time.coerce :as tc]
    [jdbc.core :as jdbc]
    [jdbc.proto :refer [ISQLType ISQLResultSetReadColumn]]
    [hikari-cp.core :refer [make-datasource]]
    [mount.core :refer [defstate]]
    [app.config :refer [conf]]
   ,))


;; https://github.com/tomekw/hikari-cp/
(comment
  (make-datasource
         {:connection-timeout 30000
          :idle-timeout 600000
          :max-lifetime 1800000
          :minimum-idle 10
          :maximum-pool-size  10
          :adapter "postgresql"
          :username "username"
          :password "password"
          :database-name "database"
          :server-name "localhost"
          :port-number 5432}))

  ; (with-open [conn (jdbc/connection ds)]
  ;   (do-stuff conn)))
;


(defstate ds
  :start
    (make-datasource (:psql conf))
  :stop
    (.close ds))


(defn dbc []
  (jdbc/connection ds))


;;; ;;; ;;; ;;;

(extend-protocol ISQLType
  ;
  org.joda.time.DateTime
  (as-sql-type [this _conn]
    (tc/to-sql-time this))
  ;
  (set-stmt-parameter! [this _conn stmt index]
    (.setTimestamp stmt index
      (tc/to-sql-time this))))


(extend-protocol ISQLResultSetReadColumn
  ;
  java.sql.Timestamp
  (from-sql-type [this _conn _metadata _index]
    (tc/from-sql-time this))
  ;
  java.sql.Date
  (from-sql-type [this _conn _metadata _index]
    (tc/from-sql-date this))
  ;
  java.sql.Time
  (from-sql-type [this _conn _metadata _index]
    (org.joda.time.DateTime. this)))
