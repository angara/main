
(ns tourserv.db
  (:require
    [monger.collection :as mc]
    [monger.query :as mq]
    [mount.core :refer [defstate]]
    ;
    [mlib.logger :refer [warn]]
    [mdb.core :refer [dbc try-warn]]))
;


(def TOURSERV_COLL "tourserv")

;; (def TOURSERV_STRUCT
;;   [ :_id    "oid"
;;     :uid    "uid"
;;     :ct     "created"
;;     :ts     "updated"
;;     ;
;;     :type   #{"auto" "equip" "apart" "guide"}
;;     :town   "Baikalsk"
;;     :addr   "Gagarina 100"
;;     :title  "serv title"
;;     :descr  "serv descr"
;;     :link   "website"
;;     :payload "number of seats in auto"
;;     :price   "from - to (rub)"
;;     ;
;;     ;; set of flags
;;     :flags  ["rawhtml"]    ; rawhtml - omit hesc
;;     ;
;;     :email ""
;;     :phone ""
;;     :person "person name"
;;     ;
;;     :note   "internal info"])


;;; ;;; ;;; ;;;

(defn tourserv-by-uid [uid]
  (try-warn
    (str "tourserv-by-uid: " uid)
    #_{:clj-kondo/ignore [:invalid-arity]}
    (mq/with-collection (dbc) TOURSERV_COLL
      (mq/find {:uid uid})
      (mq/sort {:title 1}))))
;

(defn tourserv-by-type [type & [town]]
  (try-warn
    (str "tourserv-by-type: " type)
    #_{:clj-kondo/ignore [:invalid-arity]}
    (mq/with-collection (dbc) TOURSERV_COLL
      (mq/find
        (if town
          {:type type :town town}
          {:type type}))
      (mq/sort {:title 1}))))
;

(defn insert [data]
  (try
    (mc/insert (dbc) TOURSERV_COLL data)
    (catch Exception e
      (warn "tourserv-insert:" e))))
;

;;; ;;; ;;; ;;;


(defn make-indexes [db]
  (try
    (mc/create-index db TOURSERV_COLL  (array-map :uid  1))
    (mc/create-index db TOURSERV_COLL  (array-map :type 1))
    (mc/create-index db TOURSERV_COLL  (array-map :type 1 :town 1))
    db
    (catch Exception e
      (warn "tourserv-indexes:" e))))
;


(defstate indexes
  :start
    (make-indexes (dbc)))
