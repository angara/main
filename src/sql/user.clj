(ns sql.user
  "users table (merged from mongodb user collection, 2026-10-26).
   Rows are shaped like the old mongo documents: :id (str uid),
   :username (= login), :first_name (= name), :last_name (= surname),
   :about (= note); auth keys live in user_auth."
  (:require
    [clojure.string :as s]
    [tick.core :as t]
    [honeysql.helpers :as h]
    ;
    [jdbc.core :as jdbc]
    [mlib.core :refer [to-int]]
    [mlib.logger :refer [warn]]
    [sql.conn :refer [dbc]]
    [sql.core :refer [fetch fetch-one exec]]
   ,))


(def FLDS_REQ_USER
  [:username :email :first_name :last_name :upic :allow :deny :admin])


(def SELECT_USER
  (->
    (h/select :uid :login :email :name :surname :town :note
              :upic :admin :passhash :passcode :atime :ts)
    (h/from :users)))


(defn- admin-vec [v]
  (cond
    (nil? v) nil
    (instance? java.sql.Array v) (vec (.getArray ^java.sql.Array v))
    :else (vec v)))


(defn- row->user [r]
  (when r
    (assoc r
      :id (str (:uid r))
      :username (:login r)
      :first_name (:name r)
      :last_name (:surname r)
      :about (:note r)
      :admin (admin-vec (:admin r))
      ,)))


;;; ;;; ;;; ;;;

(defn user-by-id [uid _flds]
  ;; _flds kept for signature parity; the row is tiny, always fetch all
  (try
    (row->user
      (fetch-one
        (->
          SELECT_USER
          (h/merge-where [:= :uid (to-int uid)]))))
    (catch Exception e
      (warn "user-by-id:" uid e))))


(defn users-by-ids [uids _flds]
  (when-let [uv (not-empty (vec uids))]
    (try
      (map row->user
        (fetch
          (->
            SELECT_USER
            (h/merge-where [:in :uid (map to-int uv)]))))
      (catch Exception e
        (warn "users-by-ids:" e)))))


(defn user-by-auth [auth flds]
  ;; auth key "un:login" / "em:email" -> user_auth -> users
  (when-let [a (not-empty (str auth))]
    (let [[t v] (s/split a #":" 2)]
      (when (and t (not-empty v))
        (try
          (->
            (fetch-one
              (-> (h/select :user_id)
                  (h/from :user_auth)
                  (h/where
                    [:= :auth_type t]
                    [:= :auth_id v])))
            (:user_id)
            (user-by-id flds))
          (catch Exception e
            (warn "user-by-auth:" a e)))))))


(defn user-online? [id active-duration]
  (try
    (fetch-one
      (->
        (h/select :uid)
        (h/from :users)
        (h/merge-where
          [:= :uid (to-int id)]
          [:>= :atime (t/<< (t/date-time) active-duration)])))
    (catch Exception e
      (warn "user-online?:" id e))))


;;; ;;; ;;; ;;;

(defn new-uid []
  (try
    (:nextval
      (first
        (with-open [conn (dbc)]
          (jdbc/fetch conn ["select nextval('users_uid_seq')"]))))
    (catch Exception e
      (warn "new-uid:" e))))


(def FSET_COLS
  {:first_name :name
   :last_name :surname
   :about :note})


(defn user-update [uid fset]
  (try
    (= 1
      (exec
        (->
          (h/update :users)
          (h/sset
            (into {}
              (map (fn [[k v]] [(or (FSET_COLS k) k) v]))
              fset))
          (h/where [:= :uid (to-int uid)]))))
    (catch Exception e
      (warn "user-update:" uid e))))


(defn add-auth-key [uid key-str]
  (let [[t v] (s/split key-str #":" 2)]
    (with-open [conn (dbc)]
      (jdbc/execute conn
        [(str "insert into user_auth (auth_type, auth_id, user_id) values (?,?,?)"
              " on conflict (auth_type, auth_id) do nothing")
         t v (str uid)]))))


(defn user-create [id data]
  (let [uid (to-int id)
        now (t/date-time)
        row
        {:uid uid
         :login (:username data)
         :email (str (:email data))
         :name (str (:first_name data))
         :surname (str (:last_name data))
         :town (:town data)
         :note (:about data)
         :active true
         :created (or (:ct data) now)
         :atime (or (:atime data) now)
         :ntime now
         :passhash (:passhash data)
         :passcode (:passcode data)
         :upic (:upic data)
         :ts (or (:ts data) now)}]
    (try
      (exec {:insert-into :users :values [row]})
      (doseq [a (:auth data)]
        (add-auth-key uid a))
      (assoc data :id id)
      (catch Exception e
        (warn "user-create:" id e)))))


;;; ;;; ;;; ;;;
;;; legacy webapp rows (no user_auth keys, plain-text password)

(def SELECT_USER_BY_EMLG
  (->
    (h/select :*)
    (h/from :users)
    (h/where [:= :%lower.email :?email])))


(defn active-user-by-email [eml]
  (when eml
    (fetch-one
      (h/merge-where SELECT_USER_BY_EMLG
        [:= :active :true] [:>= :uid 10])
      {:email (s/lower-case eml)})))


(defn sql-user-by-emlg [email-or-login]
  (when-let [emlg (-> email-or-login str not-empty)]
    (fetch-one
      (->
        SELECT_USER_BY_EMLG
        (h/merge-select :password)
        (h/merge-where
          [:or
            [:= :%lower.email :?emlg]
            [:= :%lower.login :?emlg]]))
      {:emlg (s/lower-case emlg)})))
