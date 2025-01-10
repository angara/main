(ns mlib.core
  (:require [clojure.string :as s]))


;; TODO: replace with (parse-long)
(defn to-int
  "returns nil or default on failure"
  ( [s]
    (to-int s nil))
  ( [s default]
    (try
      (if (string? s) (Integer/parseInt s) (int s))
      (catch Exception _ignore default))))

(defn to-float
  "returns nil or default on failure"
  ( [s]
    (to-float s nil))
  ( [s default]
    (try
      (if (string? s) (Float/parseFloat s) (float s))
      (catch Exception _ignore default))))


(defn hesc
  "Replace special characters by HTML character entities."
  [text]
  (s/escape (str text)
    {\& "&amp;" \< "&lt;" \> "&gt;" \" "&quot;" \' "&apos;"}))

