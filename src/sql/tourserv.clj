(ns sql.tourserv
  "tourserv catalog, migrated from mongodb (2026-10-26).
   Rows keep the mongo document shape: :id -> :_id, flags text[] -> vector."
  (:require
    [honeysql.helpers :as h]
    ;
    [mlib.core :refer [to-int]]
    [mlib.logger :refer [warn]]
    [sql.core :refer [fetch]]
   ,))


(defn- flags-vec [v]
  (cond
    (nil? v) nil
    (instance? java.sql.Array v) (vec (.getArray ^java.sql.Array v))
    :else (vec v)))


(defn- row->tsrv [r]
  (-> r
    (assoc :_id (:id r)
           :flags (flags-vec (:flags r)))
    (dissoc :id)))


(defn tourserv-by-uid [uid]
  (when-let [u (to-int uid)]
    (map row->tsrv
      (fetch
        (->
          (h/select :*)
          (h/from :tourserv)
          (h/where [:= :uid u])
          (h/order-by [:title :asc]))))))


(defn tourserv-by-type [type & [town]]
  (map row->tsrv
    (fetch
      (cond->
        (->
          (h/select :*)
          (h/from :tourserv)
          (h/where [:= :type (name type)]))
        town (h/merge-where [:= :town (name town)])
        true (h/order-by [:title :asc])))))
