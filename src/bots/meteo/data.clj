
(ns bots.meteo.data
  (:require
    [taoensso.timbre :refer [warn]]
    [clj-time.core :as tc]))
    ; [monger.collection :as mc]))
    ; [meteo.db :refer [db st-near] :rename {db mconn}]))
;



(defonce sess-store (atom {}))
  ;; {sid {params}}

(defn sess-params [sid]
  (get @sess-store sid))
;

(defn sess-save [sid params]
  (swap! sess-store update-in [sid] #(merge % {:ts (tc/now)} params)))
;

(defn sess-cleanup [& [time-interval]]
  (let [age (tc/minus (tc/now) (or time-interval (tc/days 3)))]
    (doseq [[cid {ts :ts}] @sess-store]
      (if (and ts (< ts age))
        (swap! sess-store dissoc cid)))))
;

;;.
