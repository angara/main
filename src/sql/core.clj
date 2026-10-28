(ns sql.core
  "Strict HoneySQL helpers. SQL failures propagate to the application boundary.
   The -on variants borrow the caller's connection and never close it."
  (:require
    [honeysql.core :as sql]
    [sql.conn :refer [dbc]]
    [sql.jdbc :as jdbc]))

(defn fetch-on [conn sqlmap & [params]]
  (jdbc/fetch conn (sql/format sqlmap :params params)))

(defn fetch-one-on [conn sqlmap & [params]]
  (jdbc/fetch-one conn (sql/format sqlmap :params params)))

(defn exec-on! [conn sqlmap & [params]]
  (jdbc/execute conn (sql/format sqlmap :params params)))

(defn fetch [sqlmap & [params]]
  (with-open [conn (dbc)]
    (fetch-on conn sqlmap params)))

(defn fetch-one [sqlmap & [params]]
  (with-open [conn (dbc)]
    (fetch-one-on conn sqlmap params)))

(defn exec [sqlmap & [params]]
  (with-open [conn (dbc)]
    (exec-on! conn sqlmap params)))

(defn next-id [tbl]
  (with-open [conn (dbc)]
    (:nextval (jdbc/fetch-one conn
                ["select nextval(?::regclass)" (str (name tbl) "_id_seq")]))))

(defn insert-into [tbl data]
  (= 1 (exec {:insert-into (keyword tbl) :values [data]})))
