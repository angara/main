
(ns meteo.api
  (:require
    [clojure.string :as s]
    [compojure.core :refer [GET ANY defroutes]]
    [clj-time.core :as tc]
    ;
    [mlib.conf :refer [conf]]
    [mlib.core :refer [to-float to-int]]
    [mlib.http :refer [json-resp text-resp]]
    ;
    [mdb.core :refer [id_id]]
    [meteo.db :refer [db st-ids st-near PUB_FIELDS]]))
;


(defn index [req]
  (let [build (:build conf)
        tearline (str (:name build) " " (:version build)
                      " b" (:num build) ". " (:timestamp build))]
    (text-resp (str "
meteo API endpoints:
---

Stations by distance -
  /meteo/st/near?ll=LNG,LAT
  /meteo/st/near?lat=LAT&lng=LNG
    default parameters: &offset=0 &limit=10

Station info -
  /meteo/st/info?st=ST1,ST2,...

Hourly aggregations -
  /meteo/st/hourly?st=ST1,ST2,...&t0=TIME0&t1=TIME1
    TIME0, TIME1 - iso formatted timestamps

---
" tearline))))
;


;; test: ll=104.27,52.28

(def ST_ALIVE (tc/days 7))

(defn q-alive []
  { :pub 1
    :ts {:$gte (tc/minus (tc/now) ST_ALIVE)}})
;

(defn ok-data [par data]
  (json-resp
    (merge par {:ok 1 :data data})))
;

;;; ;;; ;;; ;;;

(defn info [{params :params}]
  (if-let [sts (-> params :st str (s/split #",") not-empty)]
    (ok-data {}
      (map id_id (st-ids sts PUB_FIELDS)))
    ;;
    (json-resp {:err :params})))
;

(defn near [{params :params}]
  (let [[lng lat] (-> params :ll str (s/split #","))
        lng (to-float (get params :lng lng))
        lat (to-float (get params :lat lat))
        ofs (to-int (:offset params) 0)
        lim (to-int (:limit params) 10)]
    ;
    (if (and lng lat)
      (->> (st-near [lng lat] (q-alive))
        (map #(id_id (select-keys % (conj PUB_FIELDS :dist))))
        (drop ofs)
        (take lim)
        (ok-data {:lat lat :lng lng :offset ofs :limit lim}))
      ;;
      (json-resp {:err :params}))))
;

(defn hourly [{params :params}]
  (json-resp {:err :nimp}))
;


(defroutes meteo-api-routes
  (GET "/"           _  index)
  ;
  (GET "/st/info"   []  info)
  (GET "/st/near"   []  near)
  ;
  (GET "/st/hourly" []  hourly))

;

;;.
