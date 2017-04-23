
(ns misc.util
  (:require
    [clj-time.core :as tc]))
;

(def RUS_DATE_MONTHS
  [
    "Января"
    "Февраля"
    "Марта"
    "Апреля"
    "Мая"
    "Июня"
    "Июля"
    "Августа"
    "Сентября"
    "Октября"
    "Ноября"
    "Декабря"])
;

(defn rus-date [date]
  (let [t (tc/to-time-zone date (tc/default-time-zone))]
    [ (str (tc/day t))
      (get RUS_DATE_MONTHS (dec (tc/month t)))
      (str (tc/year t))]))
;

;;.
