(ns meteo.curr-temp
  (:require
    [meteo.data :refer [active-stations]]
    [meteo.fmt :refer [format-t]]
   ,))


(defn curr-temp [_]
  (when-let [{last :last} (first (active-stations))]
    (when-let [t (format-t "" (:t last) (:t_delta last))]
      [:span.t t])))
