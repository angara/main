(ns angara.lib.time
  (:import
    [java.time.format DateTimeFormatter]
    ; [java.time LocalDateTime]
   )
  )


(set! *warn-on-reflection* true)


(def df_YMD       (DateTimeFormatter/ofPattern "yyyy-MM-dd"))
(def df_ddmmyy    (DateTimeFormatter/ofPattern "dd.MM.yy"))
(def df_ddmmyyyy  (DateTimeFormatter/ofPattern "dd.MM.yyyy"))
(def df_hhmm      (DateTimeFormatter/ofPattern "HH:mm"))
(def df_hhmmss    (DateTimeFormatter/ofPattern "HH:mm:ss"))


(defn date->ymd [dt]
  (.format ^DateTimeFormatter df_YMD dt))


(defn ddmmyyyy [dt]
  (.format ^DateTimeFormatter df_ddmmyyyy dt))

(defn ddmmyy[dt]
  (.format ^DateTimeFormatter df_ddmmyy dt))

(defn hhmm [dt]
  (.format ^DateTimeFormatter df_hhmm dt))

(defn hhmmss [dt]
  (.format ^DateTimeFormatter df_hhmmss dt))


(defn ddmmyyyy-hhmm [dt]
  (str (ddmmyyyy dt) " " (hhmm dt)))
