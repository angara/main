
(ns meteo.util
  (:require
    [clojure.string :as s]
    [clj-time.core :as tc]
    ;
    [mlib.conf :refer [conf]]))
;


(def ST_PARAM  :st)
(def ST_COOKIE "meteo_st")
(def ST_MAX_NUM 50)

(def FRESH_INTERVAL (tc/minutes 60))


(defn fresh [data]
  (try
    (and
      (tc/after?
        (:ts data)
        (tc/minus (tc/now) FRESH_INTERVAL))
      data)
    (catch Exception ignore)))
;

(defn comma-split [s]
  (->>
    (s/split (s/lower-case (str s)) #"\,")
    (remove s/blank?)
    (not-empty)))
;

(defn st-param [req]
  (take ST_MAX_NUM
    (or
      (comma-split (-> req :params ST_PARAM))
      (comma-split (-> req :cookies (get ST_COOKIE) :value))
      (-> conf :meteo :st_default))))
;

;;.
