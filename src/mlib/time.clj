;; !!! deprecated !!!

(ns mlib.time
  (:require
    [clj-time.core :as t]
    [clj-time.format :as tf]))

(def tf-ddmmyyyy (tf/formatter "dd.MM.yyyy" (t/default-time-zone)))

(defn parse-ddmmyyyy [s]
  (try (tf/parse tf-ddmmyyyy (str s)) 
    (catch Exception _ignore)))

(defn ddmmyyyy [date]
  (when date 
    (try (tf/unparse tf-ddmmyyyy date) 
      (catch Exception _ignore))))
