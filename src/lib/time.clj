(ns lib.time
  (:import
   [java.time LocalDateTime])
  (:require
   [java-time.api :as jt]
   [app.config :as config]
   ,))


(defn iso->local ^LocalDateTime [s]
  (try
    (-> s (jt/instant) (jt/local-date-time config/tz))
    (catch Exception _ignore nil)))


(defn local-now ^LocalDateTime []
  (-> (jt/zoned-date-time config/tz) (jt/local-date-time)))


(def tfmt_hhmm (jt/formatter "HH:mm"))
(def tfmt_ddmmyyyy (jt/formatter "dd.MM.yyyy"))


(defn tf-hhmm [dt]
  (jt/format tfmt_hhmm dt))

(defn tf-ddmmyyyy [dt]
  (jt/format tfmt_ddmmyyyy dt))


(comment
  (iso->local "2025-01-08T15:00:00+08:00")
  ;;=> #object[java.time.LocalDateTime 0x53362301 "2025-01-08T15:00"]

  (iso->local "2025-01-08T15:00:00Z")
  ;;=> #object[java.time.LocalDateTime 0x1fb93e99 "2025-01-08T23:00"]

  (iso->local "???")
  ;;=> nil

  (tf-hhmm (iso->local "2025-01-08T15:00:00+08:00"))
  ;;=> "15:00"
  ,)
