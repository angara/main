(ns meteo.curr-temp
  (:require
    [java-time.api :as jt]
    [meteo.data :refer [active-stations]]
    [meteo.fmt :refer [format-t]]
   ,))


(defn fresh-ts? [ts]
  (when ts
    (try
      (jt/after? (jt/instant ts) 
                 (jt/minus (jt/instant) (jt/minutes 80)))
      (catch Exception _ nil)
      )))


(defn fresh-t [{last :last :as st-info}]
  (when (and (:t last)
             (fresh-ts? (:t_ts last)))
    st-info))


(defn curr-temp [_]
  (when-let [{last :last} (some fresh-t (active-stations))]
    (when-let [t (format-t "" (:t last) (:t_delta last))]
      [:span.t t])))

