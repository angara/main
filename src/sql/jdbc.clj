(ns sql.jdbc
  "Shared next.jdbc result options and raw SQL operations."
  (:require
    [next.jdbc :as jdbc]
    [next.jdbc.date-time :as date-time]
    [next.jdbc.result-set :as rs]))

(date-time/read-as-local)

;; read-as-local covers Date/Timestamp, but not SQL TIME.
(extend-protocol rs/ReadableColumn
  java.sql.Time
  (read-column-by-label [v _] (.toLocalTime v))
  (read-column-by-index [v _ _] (.toLocalTime v)))

(def options {:builder-fn rs/as-unqualified-lower-maps})

(defn fetch [conn stmt]
  (jdbc/execute! conn stmt options))

(defn fetch-one [conn stmt]
  (jdbc/execute-one! conn stmt options))

(defn execute
  "Return rows for SQL RETURNING and an affected-row count for ordinary DML."
  [conn stmt]
  (let [rows (fetch conn stmt)]
    (if (and (= 1 (count rows))
             (contains? (first rows) :next.jdbc/update-count))
      (:next.jdbc/update-count (first rows))
      rows)))
