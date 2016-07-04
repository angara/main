
(ns bots.meteo.sender
  (:require
    [taoensso.timbre :refer [warn debug]]
    [clj-time.core :as tc]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]
    [mlib.time :refer [hhmm]]
    [mlib.telegram :as tg]
    [bots.meteo.data :refer [sess-cleanup subs-hhmm]]
    [bots.meteo.stform :refer [format-st]]
    [meteo.db :refer [st-ids]]))
;


(def week-dayc {0 \0  1 \1  2 \2  3 \3  4 \4  5 \5  6 \6  7 \0})


(defn worker [apikey tm]
  (let [dc   (week-dayc (tc/day-of-week (tc/now)))
        subs (filter  #(some #{dc} (:days %))  (subs-hhmm tm))
        ids  (reduce  #(into %1 (:ids %2))  #{}  subs)
        stm  (into {} (for [s (st-ids ids)] [(:_id s) s]))]
    (doseq [s subs :let [cid (:cid s) ids (:ids s)]]
      (debug "time:" tm "subs:" (count subs) "ids:" (count ids))
      (doseq [id ids]
        (tg/send-message apikey cid
          {:text (format-st (stm id)) :parse_mode "Markdown"})))))
;

(defn minute-loop [run-flag cnf]
  (reset! run-flag true)
  (loop [prev ""]
    (let [tm (hhmm (tc/now))]
      (when (not= prev tm)
        (do
          (worker (:apikey cnf) tm)
          (sess-cleanup)))
      (when @run-flag
        (Thread/sleep (:sender-sleep cnf))
        (recur tm)))))
;

(defstate sender
  :start
    {:run-flag (atom nil)
     :thread (->
                #(minute-loop
                  (:run-flag sender)
                  (-> conf :bots :meteo38bot))
                Thread. .start)}
  :stop
    (reset! (:run-flag sender) false))


;;.
