(ns angara.webapp.db.psql
  "next.jdbc exec helpers on the shared main datasource (sql.conn/ds)."
  (:require
    [next.jdbc            :refer [execute!]]
    [next.jdbc.result-set :refer [as-unqualified-maps]]
    [next.jdbc.date-time  :refer [read-as-local]]
    ;
    [sql.conn :as sql-conn]
  ))


;; affects only next.jdbc reads (webapp queries); main's clojure.jdbc
;; protocols are untouched
(read-as-local)


;; https://www.postgresql.org/docs/current/errcodes-appendix.html
;;
(def ^:const SQL_STATE_UNIQUE_VIOLATION 23505)


(def ^:const EXEC_OPTS
  {:builder-fn as-unqualified-maps :return-keys true})


(defn exec! [stmt]
  ;; mount defstate must be derefed outside its namespace
  (execute! @sql-conn/ds stmt EXEC_OPTS))


(defn exec-one! [stmt]
  (first
    (execute! @sql-conn/ds stmt EXEC_OPTS)))
