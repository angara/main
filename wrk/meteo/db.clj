(ns meteo.db
  (:require
    [monger.core :as mg]
    [monger.collection :as mc]
    [monger.query :as mq]
    [monger.conversion :refer [from-db-object]]
    [mount.core :refer [defstate]]
    ;
    [mlib.logger :refer [warn]]
    [app.config :refer [conf]]
    [mdb.core :refer [connect disconnect try-warn]]
   ,))


(defstate mdb
  :start
    (connect (-> conf :mdb-meteo))
  :stop
    (disconnect mdb))


(defn db []
  (:db mdb))


(def PUB_FIELDS
  [:_id :ts :ll :elev :title :descr :addr :url :last :trends])


(def ST     "st")      ;; stations collection
(def HOURS  "hours")   ;; hourly collection


; (defn q-st-fresh []
;   { :pub 1
;     :ts {:$gte (tc/minus (tc/now) (tc/minutes 80))}})

;; https://docs.mongodb.com/manual/reference/command/geoNear/
;;
;; db.st.ensureIndex({ll:"2dsphere"})
;; db.st.find({ll:
;;    {$near:{$geometry:{type:"Point",coordinates:[104.2486, 52.228]}}}
;; })


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
    (mc/find-map-by-id (db) ST id (or fields PUB_FIELDS))))
;

(defn st-pub [id]
  (try-warn "st-pub:"
    (mc/find-one-as-map (db) ST {:_id id :pub 1} PUB_FIELDS)))
;


(defn st-find
  "fetch list of public stations data"
  [q & [fields]]
  (try-warn "st-by-id:"
    #_{:clj-kondo/ignore [:invalid-arity]}
    (mq/with-collection (db) ST
      (mq/find q)
      (mq/fields (or fields PUB_FIELDS))
      (mq/sort (array-map :title 1)))))
      ;(mq/limit 1000))
;


(defn st-ids
  "fetch list of public stations data"
  [ids & [fields]]
  (when ids
    (try-warn "st-by-id:"
      (mc/find-maps (db) ST
        {:_id {:$in ids} :pub 1}
        (or fields PUB_FIELDS)))))
;


(defn hourly-data [ids t0 t1 limit]
  (when ids
    (try
      #_{:clj-kondo/ignore [:invalid-arity]}
      (mq/with-collection (db) HOURS
        (mq/find {:st {:$in ids} :hour {:$gte t0 :$lt t1}})
        ;(mq/sort (array-map :hour 1))
        (mq/limit limit))
      (catch Exception e
        (warn "hourly-data:" ids t0 t1 e)))))


(defn hourly-ts0 [st_id]
  (try
    (->
      #_{:clj-kondo/ignore [:invalid-arity]}
      (mq/with-collection (db) HOURS
        (mq/find {:st st_id})
        (mq/sort (array-map :hour 1))
        (mq/limit 1))
      (first)
      (:hour))
    (catch Exception _e
      (warn "hourly-ts0:" st_id))))


(defn hourly-ts1 [st_id]
  (try
    (->
      #_{:clj-kondo/ignore [:invalid-arity]}
      (mq/with-collection (db) HOURS
        (mq/find {:st st_id})
        (mq/sort (array-map :hour -1))
        (mq/limit 1))
      (first)
      (:hour))
    (catch Exception _e
      (warn "hourly-ts1:" st_id))))
