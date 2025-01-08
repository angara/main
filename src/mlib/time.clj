
(ns mlib.time
  (:require
    [clj-time.core :as t]
    [clj-time.format :as tf]))

;; XXX: !!!

; (def tf-dmy (tf/formatter (t/default-time-zone) "dd.MM.yy" "dd.MM.yyyy" "yyyy-MM-dd"))

; (def tf-dmy (tf/formatter "dd.MM.yy" "dd.MM.yyyy" "yyyy-MM-dd"))

;;(def tf-hhmm     (tf/formatter "HH:mm" (t/default-time-zone)))
;;(def tf-hhmmss   (tf/formatter "HH:mm:ss" (t/default-time-zone)))

;;(def tf-ddmmyy   (tf/formatter "dd.MM.yy" (t/default-time-zone)))
(def tf-ddmmyyyy (tf/formatter "dd.MM.yyyy" (t/default-time-zone)))
;;(def tf-yyyymmdd (tf/formatter "yyyy-MM-dd" (t/default-time-zone)))

(def tf-ddmmyy-hhmm (tf/formatter "dd.MM.yy HH:mm" (t/default-time-zone)))
;; (def tf-ddmmyy-hhmmss (tf/formatter "dd.MM.yy HH:mm:ss" (t/default-time-zone)))

;; (def tf-yymmdd-hhmmss (tf/formatter "yyMMdd-HHmmss" (t/default-time-zone)))
;; (def tf-yyyymmdd-hhmmss (tf/formatter "yyyyMMdd-HHmmss" (t/default-time-zone)))
;; (def tf-iso-datetime (tf/formatter "yyyy-MM-dd HH:mm:ss" (t/default-time-zone)))

;; (defn parse-yyyymmdd [s]
;;   (try (tf/parse tf-yyyymmdd (str s)) 
;;     (catch Exception _ignore)))

(defn parse-ddmmyyyy [s]
  (try (tf/parse tf-ddmmyyyy (str s)) 
    (catch Exception _ignore)))

;; (defn parse-ddmmyy [s]
;;   (try (tf/parse tf-ddmmyy (str s)) 
;;     (catch Exception _ignore)))

;; (defn hhmm [date]
;;   (when date 
;;     (try (tf/unparse tf-hhmm date) 
;;       (catch Exception _ignore))))

;; (defn hhmmss [date]
;;   (when date 
;;     (try (tf/unparse tf-hhmmss date) 
;;       (catch Exception _ignore))))

;; (defn ddmmyy [date]
;;   (when date 
;;     (try (tf/unparse tf-ddmmyy date) 
;;       (catch Exception _ignore))))

(defn ddmmyyyy [date]
  (when date 
    (try (tf/unparse tf-ddmmyyyy date) 
      (catch Exception _ignore))))

(defn ddmmyy-hhmm [date]
  (when date 
    (try (tf/unparse tf-ddmmyy-hhmm date) 
      (catch Exception _ignore))))

;; (defn ddmmyy-hhmmss [date]
;;   (when date 
;;     (try (tf/unparse tf-ddmmyy-hhmmss date) 
;;       (catch Exception _ignore))))

;; (defn iso-date [date]
;;   (when date 
;;     (try (tf/unparse tf-yyyymmdd date) 
;;       (catch Exception _ignore))))

;; (defn iso-datetime [ts]
;;   (when ts 
;;     (try (tf/unparse tf-iso-datetime ts) 
;;       (catch Exception _ignore))))

