(ns meteo.fmt
  (:import
    [java.util Locale])
  (:require
    [clojure.math :refer [round floor]]
    [clojure.string :as s]
   ,))


(def HPA_MMHG 1.3332239)


(defn strip-zeros [s]
  (if-let [z (re-matches #".+?(0+)" s)]
    (subs s 0 (-
                (.length s)
                (.length (second z))))
    s))


(defn strip-dot [v]
  (if (s/ends-with? v ".")
    (subs v 0 (-> v .length dec))
    v))


(defn nfix1 [x]
  (String/format Locale/ROOT "%.1f" (to-array [(float x)])))


;; (defn nfix2 [x]
;;   (String/format Locale/ROOT "%.2f" (to-array [(float x)])))


(defn nf1 [x]
  (try
    (-> x nfix1 strip-zeros strip-dot)
    (catch Exception 
      _ignore)))


;; (defn nf2 [x]
;;   (try
;;     (-> x nfix2 strip-zeros strip-dot)
;;     (catch Exception 
;;       _ignore)))


(defn wind-nesw [b]
  (try
    (get
      ["С","СВ","В","ЮВ","Ю","ЮЗ","З","СЗ"]
      (int (floor (mod (/ (+ b 22) 45) 8))))
    (catch Exception 
      _ignore)))


(defn format-w [pref w g b]
  (when w
    (let [w (nf1 w)
          g (nf1 g)
          res (str pref "<b>" w "</b>")
          res (if (and g (not= g w))
                (str res "<b>-" g "</b>" " м/с")
                (str res " м/с"))
          res (str "<nobr>" res "</nobr>")
          dir (wind-nesw b)]
      (if dir
        (str res ", " "<b>" dir "</b>")
        res))))


(defn format-h [pref h]
  (when h
    (str pref "<nobr><b>" (round (float h)) "</b>%</nobr>")))


(defn format-p [pref p]
  (when p
    (str pref "<nobr><b>" (round (/ p HPA_MMHG)) "</b> мм.рт.ст</nobr>")))


(defn format-t [pref t delta]
  (try
    (let [t (Math/round (float t))
          [cls sign]  (cond
                        (< 0 t) ["pos" "+"]
                        (> 0 t) ["neg" "-"]
                        :else   ["zer" ""])
          [trc arr]   (if delta
                        (cond
                          (> delta  0.8) ["pos" "&uarr;"]
                          (< delta -0.8) ["neg" "&darr;"]
                          :else          [""    "&nbsp;"])
                        ["" "&nbsp;"])]
      (list
       pref
       [:span {:class cls} sign [:b (Math/abs t)]]
       [:span {:class (str "arr " trc)} arr]
       "&deg;C" 
       ))
      ;
    (catch Exception 
      _ignore)))

