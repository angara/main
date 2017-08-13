
(ns misc.util
  (:require
    [clj-time.core :as tc]))
;

(def RUS_MONTHS
  [
    "январь"
    "февраль"
    "март"
    "апрель"
    "май"
    "июнь"
    "июль"
    "август"
    "сентябрь"
    "октябрь"
    "ноябрь"
    "декабрь"])
;

(def RUS_MONTHS_FC
  [
    "Январь"
    "Февраль"
    "Март"
    "Апрель"
    "Май"
    "Июнь"
    "Июль"
    "Август"
    "Сентябрь"
    "Октябрь"
    "Ноябрь"
    "Декабрь"])
;

(def RUS_DATE_MONTHS
  [
    "января"
    "февраля"
    "марта"
    "апреля"
    "мая"
    "июня"
    "июля"
    "августа"
    "сентября"
    "октября"
    "ноября"
    "декабря"])
;

(def RUS_DATE_MONTHS_FC
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
