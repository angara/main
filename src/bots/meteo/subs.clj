
(ns bots.meteo.subs
  (:require
    [clojure.string :as s]
    [taoensso.timbre :refer [debug]]
    [clj-time.core :as tc]
    [clj-time.local :refer [local-now]]
    [mlib.core :refer [to-int]]
    [mlib.time :refer [hhmm]]
    [mlib.telegram :as tg]
    [bots.meteo.util :refer [apikey cid gmaps-link inkb wd-map]]
    [meteo.db :refer [st-ids]]
    [bots.meteo.data :refer [get-favs get-subs subs-add!]]))
    ;   [sess-params sess-save get-favs favs-add! favs-del!]]
    ; [bots.meteo.util :refer [format-st]]))
;


(def SUBS-MAX 20)

;; {_id, ts, cid:cid, ord:ord,
;;   time:"16:45", days:"01233456", ids:["uiii","npsd",...] }


(defn kbd [{:keys [ord time days]}]
  (let [cmd (str "sbed " ord " ")]
    {:inline_keyboard
      [
        [{:text time :callback_data (str cmd (inkb))}]
        [
          {:text "<<" :callback_data (str cmd "h-")}
          {:text "<"  :callback_data (str cmd "m-")}
          {:text ">"  :callback_data (str cmd "m+")}
          {:text ">>" :callback_data (str cmd "h+")}]
        (for [d "1234560"]
          {:text (wd-map d) :callback_data (str cmd "d" d)})]}))
;


(defn subs-edit [cid ord]
  (when-let [sb (first (get-subs cid ord))]
    (let [sts (st-ids (:ids sb) [:title])]
      (prn "sts:" sts)
      (prn "kbd:" (kbd sb))
      (tg/send-message apikey cid
        { :text (s/join "\n" (map :title sts))
          :parse_mode "Markdown"
          :reply_markup (kbd sb)}))))
;

(defn on-sbed [msg par params]
  (if-let [ord (to-int par)]
    (let [cmd (first params)]
      (prn "sbed:" ord params))
    (debug "sbed: !ord " par)))
;

(defn cmd-subs [msg par]
  (let [cid (cid msg)]
    (doseq [s (get-subs cid)]
      (subs-edit cid (:ord s)))))
;

(defn cmd-adds [msg par]
  (let [cid (cid msg)
        favs (not-empty (get-favs cid))
        subs (not-empty (get-subs cid))]
    (cond
      (not favs)
      (tg/send-text apikey cid "Ничего нет в Избранном!")

      (<= SUBS-MAX (count subs))
      (tg/send-text apikey cid "Больше рассылок добавить нельзя!")

      :else
      (let [ord (inc (apply max (cons 0 (map :ord subs))))
            hrs (format "%02d" (tc/hour (local-now)))]
        (subs-add! cid ord (str hrs ":00") "0123456" favs)
        (subs-edit cid ord)))))
;


;;.
