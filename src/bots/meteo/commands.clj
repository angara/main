

(ns bots.meteo.commands
  (:require
    [taoensso.timbre :refer [warn]]
    [mlib.conf :refer [conf]]
;    [mlib.core :refer [to-int]]
    [mlib.telegram :as tg]))
;


(defn on-message [msg]
  (prn "msg:" msg))
;

(defn on-callback [cbq]
  (prn "cbq:" cbq))
;

;;.
