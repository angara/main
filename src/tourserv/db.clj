
(ns tourserv.db
  (:require
    [monger.collection :as mc]
    [monger.query :as mq]
    [mount.core :refer [defstate]]
    ;
    [mlib.log :refer [warn]]
    [mlib.conf :refer [conf]]
    [mdb.core :refer [dbc try-warn]]))
;


(def TOURSERV_COLL "tourserv")

(def TOURSERV_STRUCT
  [ :_id    "oid"
    :uid    "uid"
    :ct     "created"
    :ts     "updated"
    ;
    :type   #{"auto" "equip" "apart" "guide"}
    :town   "Baikalsk"
    :addr   "Gagarina 100"
    :title  "serv title"
    :descr  "serv descr"
    :link   "website"
    :payload "number of seats in auto"
    :price   "from - to (rub)"
    ;
    :email ""
    :phone ""
    :person "person name"
    ;
    :note   "internal info"])
;

;;; ;;; ;;; ;;;

(defn tourserv-by-uid [uid]
  (try-warn
    (str "tourserv-by-uid: " uid)
    (mq/with-collection (dbc) TOURSERV_COLL
      (mq/find {:uid uid})
      (mq/sort {:title 1}))))
;

(defn tourserv-by-type [type & [town]]
  (try-warn
    (str "tourserv-by-type: " type)
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


(defn indexes [db]
  (try
    (mc/create-index db TOURSERV_COLL  (array-map :uid  1))
    (mc/create-index db TOURSERV_COLL  (array-map :type 1))
    (mc/create-index db TOURSERV_COLL  (array-map :town 1))
    true
    (catch Exception e
      (warn "tourserv-indexes:" e))))
;


(defstate indexes
  :start
    (indexes (dbc)))
;

;;.


;;; services.yaml converter ;;;
; (defn yaml-edn [y]
;   (let [t (dissoc (assoc y :town (:group y))
;             :id :group :rating :update :updated :nick :icq :skype :orig)]
;     (into {} (filter #(second %) t)))
; ;
;
;   (map
;     (comp insert yaml-edn)
;     (yaml/parse-string (slurp "../wrk/services.yaml"))))
