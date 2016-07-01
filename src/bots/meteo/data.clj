
(ns bots.meteo.data
  (:require
    [taoensso.timbre :refer [warn]]
    [mount.core :refer [defstate]]
    [clj-time.core :as tc]
    [monger.collection :as mc]
    [monger.query :as mq]

    [mlib.core :refer [try-warn]]
    [mdb.core :refer [dbc]]))
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
      (when (and ts (< ts age))
        (swap! sess-store dissoc cid)))))
;


(def LOG-COLL "mbot_log")
;; {ts ll[]
;;  data
;; TODO

(def FAVS-COLL "mbot_favs")
;; {_id: cid, ts:ts, favs:[...]}


(def SUBS-COLL "mbot_subs")
;; {_id, ts, cid:cid, ord:ord,
;;   time:"16:45", days:"01233456", ids:["uiii","npsd",...] }
;; idx: cid, idx: time


(defn mbot-log [msg]
  (let [{lat :latitude lng :longitude} (-> msg :message :location)
        ll (when (and lat lng) {:ll [lng lat]})]
    (try
      (mc/insert (dbc) LOG-COLL (merge {:ts (tc/now) :data msg} ll))
      (catch Exception e
        (warn "mbot-log:" e)))))
;

(defn ensure-indexes []
  (mc/ensure-index (dbc) LOG-COLL  (array-map :ts 1))
  (mc/ensure-index (dbc) LOG-COLL  (array-map :ll "2dsphere"))
  ;
  (mc/ensure-index (dbc) SUBS-COLL (array-map :cid  1))
  (mc/ensure-index (dbc) SUBS-COLL (array-map :time 1)))
;

;; ;; ;; ;; ;;

(defn get-favs [cid]
  (try-warn "get-favs:"
    (:favs (mc/find-map-by-id (dbc) FAVS-COLL cid))))
;

(defn favs-add! [cid st-id]
  (let [favs (remove #{st-id} (get-favs cid))]
    (try-warn "favs-add:"
      (mc/update-by-id (dbc) FAVS-COLL cid
        {:$set {:ts (tc/now) :favs (conj (vec favs) st-id)}}
        {:upsert true}))))
;

(defn favs-del! [cid st-id]
  (try-warn "favs-del:"
    (mc/update-by-id (dbc) FAVS-COLL cid
      {:$pull {:favs st-id} :$set {:ts (tc/now)}})))
;

;; ;; ;; ;; ;;

(defn get-subs [cid & [ord]]
  (try-warn "get-subs"
    (mq/with-collection (dbc) SUBS-COLL
      (mq/find (if ord {:cid cid :ord ord} {:cid cid}))
      (mq/sort (array-map :ord 1)))))
;

(defn subs-add! [cid ord time days ids]
  (try-warn "subs-add"
    (mc/insert (dbc) SUBS-COLL
      {:ts (tc/now) :cid cid :ord ord :time time :days days :ids ids})))
;

;; ;; ;; ;; ;;

(defstate data
  :start
    (ensure-indexes))
;

;;.
