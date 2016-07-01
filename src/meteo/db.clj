
(ns meteo.db
  (:require
    [taoensso.timbre :refer [warn]]
    [monger.core :as mg]
    [monger.collection :as mc]
    [monger.query :as mq]
    [monger.conversion :refer [from-db-object]]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]
    [mdb.core :refer [connect disconnect]]
    [db.util :refer [try-warn]]))
;

(defstate mdb
  :start
    (-> conf :db :meteo connect)
  :stop
    (disconnect mdb))
;

(defn db []
  (:db mdb))


(def ST "st")   ;; stations collection


; (defn q-st-fresh []
;   { :pub 1
;     :ts {:$gte (tc/minus (tc/now) (tc/minutes 80))}})

;;
;; db.st.ensureIndex({ll:"2dsphere"})
;; db.st.find({ll:
;;    {$near:{$geometry:{type:"Point",coordinates:[104.2486, 52.228]}}}
;; })
;
; https://docs.mongodb.com/manual/reference/command/geoNear/#dbcmd.geoNear
;
; db.runCommand({
;   geoNear: "st",
;   spherical: true,
;   near: {type:"Point", coordinates:[104.2486, 52.228]}
;   query: {pub:1}
;   limit: 100
; })

; https://docs.mongodb.com/manual/tutorial/calculate-distances-using-spherical-geometry-with-2d-geospatial-indexes/

; (def distance-multiplier-mi 3963.1906)
; (def distance-multiplier-km 6378.1370)


;; http://www.movable-type.co.uk/scripts/latlong.html


; db.location_data.aggregate(
;     {$geoNear:{}
;         near : {type : 'Point', coordinates : [127.0189206,37.5168266]},
;         distanceField : 'distance',
;         spherical : true,
;         maxDistance : 2000} )


(defn st-near
  "returns [{:dis M :obj {:_id ... :ll [long lat} ...} ...]"
  [ll query]
  (try-warn "st-near:"
    (let [res (from-db-object
                (mg/command (db)
                  (array-map
                    :geoNear ST
                    :near {:type "Point" :coordinates ll}
                    :spherical true
                    :query query
                    :limit 1000))
                true)]
      (if (< 0 (:ok res))
        (map #(assoc (:obj %) :dist (:dis %)) (:results res))
        (warn "st-near:" ll res)))))
;

(defn st-by-id [id & [fields]]
  (try-warn "st-by-id:"
    (mc/find-map-by-id (db) ST id (or fields []))))
;


(defn st-find
  "fetch list of public stations data"
  [q & [fields]]
  (try-warn "st-by-id:"
    (mq/with-collection (db) ST
      (mq/find q)
      (mq/fields (or fields []))
      (mq/sort (array-map :title 1)))))
      ;(mq/limit 1000))
;


(defn st-ids
  "fetch list of public stations data"
  [ids & [fields]]
  (try-warn "st-by-id:"
    (mc/find-maps (db) ST {:_id {:$in ids} :pub 1} (or fields []))))
;


;;.
