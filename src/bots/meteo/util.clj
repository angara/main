
(ns bots.meteo.util
  (:require
    [clojure.string :as s]
    [clj-time.core :as tc]
    [mlib.time :refer [hhmm ddmmyy]]))
;

(defn hpa-mmhg [h]
  (when h (/ h 1.3332239)))
;

(defn wind-rhumb [b]
  (when (and b (<= 0 b) (< b 360))
    (let [rh (int (mod (Math/floor (/ (+ b 22) 45)) 8))]
      (["С" "СВ" "В" "ЮВ" "Ю" "ЮЗ" "З" "СЗ"] rh))))
;

(defn tmf [ts]
  (if (tc/before? ts (tc/minus (tc/now) (tc/hours 12)))
    (str (ddmmyy ts) " " (hhmm ts))
    (hhmm ts)))
;

(defn num [x]
  (let [n (format "%.1f"  (float x))]
    (if-let [m (re-matches #"^(.+)(\.0*)$" n)]
      (m 1)
      n)))
;

(defn t-plus [t]
  (if (< 0 t)
    (str "+" (num t))
    (num t)))
;

(defn format-t [t]
  (when t
    (str "Температура: *" (t-plus t) "* \u00b0C\n")))

(defn format-h [h]
  (when h
    (str "Влажность: *" (num h) "* %\n")))

(defn format-p [p]
  (when p
    (str "Давление: *" (num (hpa-mmhg p)) "* мм.рт\n")))

(defn format-wind [w g b]
  (when w
    (str
      "Ветер: *" (num w) (when g (str "-" (num g))) "* м/с"
      (when-let [r (wind-rhumb b)] (str " (*" r "*)"))
      "\n")))
;

(defn format-water [t l]
  (when (or t l)
    (str "Вода:"
      (when t (str " *" (t-plus t) "* \u00b0C"))
      (when l (str " *" (num l) "* м"))
      "\n")))
;

(defn format-st [st & [dis]]
  (when (and st (:pub st))
    (let [data (:last st)
          ts (:ts data)
          fresh (tc/minus (tc/now) (tc/minutes 70))]
      ;; clock: "\uD83D\uDD51"
      (str "*" (:title st) "*" "  '" (tmf ts) "\n"
        (when-let [d (:descr st)] (str d "\n"))
        (when-let [a (:addr st)]  (str a "\n"))
        (when dis (str (format "(%.1f км)" (/ dis 1000)) "\n"))
        ;"_- " (hhmm ts) " -_" "\n"
        "\n"
        (when (and ts (tc/after? ts fresh))
          (str
            (format-t (:t data))
            (format-h (:h data))
            (format-p (:p data))
            (format-wind (:w data) (:g data) (:b data))
            (format-water (:wt data) (:wl data))))))))
;

;;.
