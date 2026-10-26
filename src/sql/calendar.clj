(ns sql.calendar
  "events calendar, migrated from mongodb (2026-10-26).
   Rows keep the mongo document shape: :id -> :_id."
  (:require
    [tick.core :as t]
    [honeysql.helpers :as h]
    ;
    [mlib.core :refer [to-int hexbyte urand-bytes]]
    [sql.core :refer [fetch fetch-one exec]]
   ,))


(def CRECS_FETCH_LIMIT (int 1000))

(def STATUS_PUBL "publ")


(defn- row->crec [r]
  (-> r
    (assoc :_id (:id r))
    (dissoc :id)))


(defn- new-crec-id []
  ;; 24-hex string, same shape the mongo ObjectId had
  (apply str (map hexbyte (urand-bytes 12))))


;;; ;;; ;;; ;;;

(defn crec-by-id [id]
  (when-let [r (fetch-one
                 (->
                   (h/select :*)
                   (h/from :calendar)
                   (h/where [:= :id (str id)])))]
    (row->crec r)))


(defn crec-by-id-uid [id uid]
  (when-let [r (fetch-one
                 (->
                   (h/select :*)
                   (h/from :calendar)
                   (h/where
                     [:= :id (str id)]
                     [:= :uid (to-int uid)])))]
    (row->crec r)))


(defn crec-update [id upd]
  (= 1
    (exec
      (->
        (h/update :calendar)
        (h/sset upd)
        (h/where [:= :id (str id)])))))


(defn crecs-by-uid [uid]
  (map row->crec
    (fetch
      (->
        (h/select :*)
        (h/from :calendar)
        (h/where [:= :uid (to-int uid)])
        (h/order-by [:date :desc])
        (h/limit CRECS_FETCH_LIMIT)))))


(defn crecs-all []
  (map row->crec
    (fetch
      (->
        (h/select :*)
        (h/from :calendar)
        (h/order-by [:date :desc])
        (h/limit CRECS_FETCH_LIMIT)))))


(defn crecs-publ []
  (let [start (t/<< (t/date-time) (t/of-hours 20))]
    (map row->crec
      (fetch
        (->
          (h/select :*)
          (h/from :calendar)
          (h/where
            [:>= :date start]
            [:= :status STATUS_PUBL])
          (h/order-by [:date :asc])
          (h/limit CRECS_FETCH_LIMIT))))))


;;; ;;; ;;; ;;;

(defn add-crec [crec]
  (let [id (new-crec-id)]
    (exec
      (->
        (h/insert-into :calendar)
        (h/values
          [(-> crec
             (assoc :id id :uid (to-int (:uid crec)))
             (select-keys
               [:id :uid :ct :ts :date :date_1 :status
                :title :descr :thumb :link]))])))
    (assoc crec :_id id)))
