(ns lib.time
  (:require
   [java-time.api :as jt]
   [app.config :as config]
   ,))


(defn iso->local [s]
  (try
    (-> s (jt/instant) (jt/local-date-time config/tz))
    (catch Exception _ignore)))


(def tfmt_hhmm (jt/formatter "HH:mm"))


(defn tf-hhmm [dt]
  (jt/format tfmt_hhmm dt))


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
