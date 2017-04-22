
(ns calendar.db
  (:require
    [monger.collection :as mc]
    [monger.query :as mq]
    [mount.core :refer [defstate]]
    ;
    [mlib.log :refer [warn]]
    [mlib.conf :refer [conf]]
    [mdb.core :refer [dbc try-warn new_id oid]]))
;


(def CALENDAR_COLL "calendar")

(def CRECS_FETCH_LIMIT (int 1000))


(def CALENDAR_STRUCT
  [ :_id    "oid"
    :uid    "uid"
    :ct     "created"
    :ts     "updated"

    :date   "event date"
    :date_1 "optional end date"

    :status ""    ;; :new :apply :publ :canc  :removed

    :title  ""
    :descr  ""
    :thumb  ""    ;; thumbnail-100 uri

    :link   ""

    :tags   []])
;


(defn crec-by-id [id]
  (try-warn (str "crec-by-id: " id)
    (mc/find-by-id (dbc) CALENDAR_COLL (oid id))))
;

(defn crec-by-id-uid [id uid]
  (try-warn "crec-by-id-uid: " id uid
    (mc/find-one-as-map (dbc) CALENDAR_COLL
      {:_id (oid id) :uid uid})))
;

(defn crec-update [id upd]
  (let [_id (oid id)]
    (try-warn (str "crec-update: " id)
      (->
        (mc/update (dbc) CALENDAR_COLL
          {:_id _id}
          {:$set upd})
        (.getN)
        (= 1)))))
;

(defn crecs-by-uid [uid]
  (try-warn (str "recs-by-uid: " uid);
    (mq/with-collection (dbc) CALENDAR_COLL
      (mq/find {:uid uid})
      (mq/sort {:date -1})
      (mq/limit CRECS_FETCH_LIMIT))))
;


(defn add-crec [crec]
  (let [_id (new_id)]
    (try-warn "add-crec:"
      (mc/insert (dbc) CALENDAR_COLL
        (assoc crec :_id _id))
      crec)))
;


(defn make-indexes [db]
  (try
    (mc/create-index db CALENDAR_COLL  (array-map :uid  1))
    (mc/create-index db CALENDAR_COLL  (array-map :date -1))
    ; (mc/create-index db TOURSERV_COLL  (array-map :type 1))
    ; (mc/create-index db TOURSERV_COLL  (array-map :type 1 :town 1))
    db
    (catch Exception e
      (warn "calendar-indexes:" e))))
;


(defstate indexes
  :start
    (make-indexes (dbc)))
;

;;.
