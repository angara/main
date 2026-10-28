(ns angara.webapp.db.psql
  "next.jdbc exec helpers on the shared main datasource (sql.conn/ds)."
  (:require
    [next.jdbc            :refer [execute!]]
    [sql.jdbc :as jdbc]
    ;
    [sql.conn :as sql-conn]
  ))


;; https://www.postgresql.org/docs/current/errcodes-appendix.html
;;
(def ^:const SQL_STATE_UNIQUE_VIOLATION 23505)


(def ^:const EXEC_OPTS
  (assoc jdbc/options :return-keys true))


(defn exec! [stmt]
  (execute! (sql-conn/datasource) stmt EXEC_OPTS))


(defn exec-one! [stmt]
  (first
    (execute! (sql-conn/datasource) stmt EXEC_OPTS)))
