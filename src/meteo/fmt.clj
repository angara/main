
(ns meteo.fmt
  (:import
    [java.util Locale]))
;


(def HPA_MMHG 1.3332239)

(defn nf1 [x]
  (String/format Locale/ROOT "%.1f" (to-array [(float x)])))
;

(defn nf2 [x]
  (String/format Locale/ROOT "%.2f" (to-array [(float x)])))
;

(defn wind-nesw [b]
  (try
    (get
      ["С","СВ","В","ЮВ","Ю","ЮЗ","З","СЗ"]
      (int (Math/floor (mod (/ (+ b 22) 45) 8))))
    (catch Exception ignore)))
;

(defn format-w [w g b]
  (when w
    (let [res (str "Ветер: " "<b>" (Math/round (float w)) "</b>")
          res (if g
                (str res "<b>-" (Math/round (float g)) "</b>" " м/с")
                (str res " м/с"))
          res (str "<nobr>" res "</nobr>")
          dir (wind-nesw b)]
      (if dir
        (str res ", " "<b>" dir "</b>")
        res))))
;

(defn format-h [h]
  (when h
    (str "Влажность: <nobr><b>" (Math/round (float h)) "</b> %</nobr>")))
;

(defn format-p [p]
  (when p
    (str "Давление: <nobr><b>"
      (Math/round (/ p HPA_MMHG))
      "</b> мм.рт.ст</nobr>")))
;

(defn format-t [t avg]
  (try
    (let [t (Math/round (float t))
          [cls sign]  (cond
                        (< 0 t) ["pos" "+"]
                        (> 0 t) ["neg" "-"]
                        :else   ["zer" ""])
          [trc arr]   (when avg
                        (cond
                          (> t (+ avg 1)) ["pos" "&uarr;"]
                          (< t (- avg 1)) ["neg" "&darr;"]
                          :else           [""    "&nbsp;"]))]
      (list
        [:span {:class cls} sign [:i (Math/abs t)]]
        "&deg;"
        [:span {:class (str "arr " trc)} arr]))
      ;
    (catch Exception ignore)))
;

(defn format-wt [wt wl]
  (try
    (when wt
      (str "Температура воды: <nobr><b>"
            (Math/round (float wt)) "</b>&deg;</nobr>"
        (when wl
          (str ", <nobr>уровень <b>" (nf2 wl) "</b> м</nobr>"))))
    (catch Exception ignore)))
;

;;.
