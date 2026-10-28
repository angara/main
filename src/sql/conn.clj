(ns sql.conn
  (:require
    [jdbc.core :as jdbc]
    [jdbc.proto :refer [ISQLType ISQLResultSetReadColumn]]
    [hikari-cp.core :refer [make-datasource]]
    [mount.core :refer [defstate]]
    [app.config :refer [conf]]
   ,)
  (:import
    (java.sql Timestamp)
    (java.time Instant LocalDateTime ZoneId)))


;; https://github.com/tomekw/hikari-cp/
;;
;; pool size: the site runs ~150 concurrent Tomcat threads and the webapp
;; serves forum/api traffic, so a 10-connection pool starves under load
;; ("Connection is not available, request timed out after 30000ms").
;; pg max_connections is 60, so 20 stays well inside the budget.
(def ^:private hikari-opts
  { :maximum-pool-size  20
    :minimum-idle       5
    :connection-timeout 30000
    :idle-timeout       600000
    :max-lifetime       1800000
    })

(comment
  (make-datasource
         (merge hikari-opts
                { :adapter "postgresql"
                  :username "username"
                  :password "password"
                  :database-name "database"
                  :server-name "localhost"
                  :port-number 5432})))

  ; (with-open [conn (jdbc/connection ds)]
  ;   (do-stuff conn)))
;


(defstate ds
  :start
    (make-datasource (merge hikari-opts (:psql conf)))
  :stop
    (.close ds))


(defn dbc []
  (jdbc/connection ds))


(defn datasource []
  ;; for next.jdbc users outside this ns (mount defstate var must not
  ;; be deref'd externally)
  ds)


;;; ;;; ;;; ;;;

;; java.time adapters (jdbc 4.2: pgjdbc takes LocalDate/LocalDateTime natively,
;; but NOT Instant — go through Timestamp). Reads come back as java.time too.

(defn- ldt->timestamp ^Timestamp [^LocalDateTime ldt]
  (Timestamp/from (.toInstant (.atZone ldt (ZoneId/systemDefault)))))


(extend-protocol ISQLType
  LocalDateTime
  (as-sql-type [this _conn]
    (ldt->timestamp this))
  (set-stmt-parameter! [this _conn ^java.sql.PreparedStatement stmt ^Long index]
    (.setTimestamp stmt index (ldt->timestamp this)))

  Instant
  (as-sql-type [this _conn]
    (Timestamp/from this))
  (set-stmt-parameter! [this _conn ^java.sql.PreparedStatement stmt ^Long index]
    (.setTimestamp stmt index (Timestamp/from this))))


(extend-protocol ISQLResultSetReadColumn
  java.sql.Timestamp
  (from-sql-type [this _conn _metadata _index]
    (LocalDateTime/ofInstant (.toInstant this) (ZoneId/systemDefault)))
  ;
  java.sql.Date
  (from-sql-type [this _conn _metadata _index]
    (.toLocalDate this))
  ;
  java.sql.Time
  (from-sql-type [this _conn _metadata _index]
    (.toLocalTime this)))
