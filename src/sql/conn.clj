(ns sql.conn
  (:require
    [next.jdbc :as jdbc]
    [sql.jdbc]
    [hikari-cp.core :refer [make-datasource]]
    [mount.core :refer [defstate]]
    [app.config :refer [conf]]
   ,))


;; https://github.com/tomekw/hikari-cp/
;;
;; pool size: the site runs ~150 concurrent Tomcat threads and the webapp
;; serves forum/api traffic, so a 10-connection pool starves under load
;; ("Connection is not available, request timed out after 30000ms").
;; Connection budget: main 20 + Tomcat 20 out of pg max_connections=60;
;; leave the remainder for other clients, maintenance and reserved slots.
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

  ; (with-open [conn (jdbc/get-connection ds)]
  ;   (do-stuff conn)))
;


(defstate ds
  :start
    (make-datasource (merge hikari-opts (:psql conf)))
  :stop
    (.close ds))


(defn dbc []
  (jdbc/get-connection ds))


(defn datasource []
  ;; for users outside this ns (mount defstate var must not
  ;; be deref'd externally)
  ds)
