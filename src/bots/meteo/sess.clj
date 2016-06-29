
(ns bots.meteo.sess)


(defonce store (atom {}))
  ;; {sid {params}}


(defn params [sid]
  (get @store sid))
;

(defn save [sid params]
  (swap! store update-in [sid] #(merge % params)))
;

;;.
