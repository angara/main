(ns mdb.user
  (:require
    [mlib.logger :refer [warn]]
    [clj-time.core :as tc]
    [mount.core :refer [defstate]]
    [monger.collection :as mc]
    ;
    [mdb.core :refer [dbc id_id next-sn]]
   ,))


(def user-coll "user")
(def UID_SEQ  "uid")

;; db.seq.insert({_id:"uid",n:100000})


(def FLDS_REQ_USER
  [:username :email :first_name :last_name :upic :allow :deny :admin])


(defn create-indexes [db]
  (try
    (mc/create-index db user-coll
      (array-map :auth 1)
      { :unique true
        :partialFilterExpression {:auth {:$exists true}}})
    (catch Exception e (warn "create-indexes:" e))))


;;; user

(defn new-uid []
  (next-sn UID_SEQ))


(defn user-by-id [uid flds]
  (when uid
    (try
      (id_id (mc/find-map-by-id (dbc) user-coll uid flds))
      (catch Exception e (warn "user-by-id:" e)))))


(defn users-by-ids [uids flds]
  (when-let [uv (not-empty (vec uids))]
    (try
      (map id_id (mc/find-maps (dbc) user-coll {:_id {:$in uv}} flds))
      (catch Exception e (warn "users-by-ids:" e)))))


(defn user-by-auth [auth flds]
  (when-let [a (str auth)]
    (try
      (id_id (mc/find-one-as-map (dbc) user-coll {:auth a} flds))
      (catch Exception e (warn "db/user-by-auth:" e)))))


(defn user-create [id data]
  (try
    (let [now (tc/now)]
      (mc/insert (dbc) user-coll
        (merge {:_id id :ct now :atime now :ts now} data))
      (assoc data :id id))
    (catch Exception e (warn "db/user-create:" e))))


(defn user-update [uid fset]
  (try
    (= 1 (.getN (mc/update-by-id (dbc) user-coll uid {:$set fset})))
    (catch Exception e (warn "db/user-update:" e))))


(defstate indexes
  :start (create-indexes (dbc)))


;  _id
;  auth ["un:login-lower" "em:email-norm" "vk:123" "fb:456"]  ;; indexed unique
;  email "user@host.dom"
;  username "nick name / login"
;  passhash "bcrypt"
;  passcode "password reset code"
;  first_name "name"
;  last_name "family"
;  about "user note"
;  upic "upic url 100 px"
;  allow {}
;  deny {}
;  admin ["bb"]
