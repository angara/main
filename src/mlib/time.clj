
(ns mlib.time
  (:import
    (java.time LocalDate LocalDateTime)
    (java.time.format DateTimeFormatter)))


(defn now-ms [] (System/currentTimeMillis))
(defn now-date [] (java.util.Date.))

;; DateTimeFormatter is immutable/thread-safe; patterns are wall-clock
;; (LocalDateTime in the default zone, same semantics as the joda version).
;; Parsers return LocalDateTime at start of day — same as joda date-only parse.

(def tf-hhmm     (DateTimeFormatter/ofPattern "HH:mm"))
(def tf-hhmmss   (DateTimeFormatter/ofPattern "HH:mm:ss"))

(def tf-ddmmyy   (DateTimeFormatter/ofPattern "dd.MM.yy"))
(def tf-ddmmyyyy (DateTimeFormatter/ofPattern "dd.MM.yyyy"))
(def tf-yyyymmdd (DateTimeFormatter/ofPattern "yyyy-MM-dd"))

(def tf-ddmmyy-hhmm   (DateTimeFormatter/ofPattern "dd.MM.yy HH:mm"))
(def tf-ddmmyy-hhmmss (DateTimeFormatter/ofPattern "dd.MM.yy HH:mm:ss"))

(def tf-yymmdd-hhmmss   (DateTimeFormatter/ofPattern "yyMMdd-HHmmss"))
(def tf-yyyymmdd-hhmmss (DateTimeFormatter/ofPattern "yyyyMMdd-HHmmss"))
(def tf-iso-datetime    (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm:ss"))


(defn- parse-date ^LocalDateTime [^DateTimeFormatter fmt ^String s]
  (try
    (.atStartOfDay (LocalDate/parse s fmt))
    (catch Exception _ignore nil)))


(defn parse-yyyymmdd [s]
  (parse-date tf-yyyymmdd (str s)))


(defn parse-ddmmyy [s]
  (parse-date tf-ddmmyy (str s)))


(defn parse-ddmmyyyy [s]
  (parse-date tf-ddmmyyyy (str s)))


(defn- fmt [^DateTimeFormatter f date]
  (when date (try (.format f date) (catch Exception _ignore nil))))


(defn hhmm [date]      (fmt tf-hhmm date))
(defn hhmmss [date]    (fmt tf-hhmmss date))
(defn ddmmyy [date]    (fmt tf-ddmmyy date))
(defn ddmmyyyy [date]  (fmt tf-ddmmyyyy date))

(defn ddmmyy-hhmm [date]   (fmt tf-ddmmyy-hhmm date))
(defn ddmmyy-hhmmss [date] (fmt tf-ddmmyy-hhmmss date))

(defn iso-date [date]
  (fmt tf-yyyymmdd date))

(defn iso-datetime [ts]
  (fmt tf-iso-datetime ts))

;;.
