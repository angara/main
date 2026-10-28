(ns sql.user
  "users table (merged from mongodb user collection, 2026-10-26).
   Rows are shaped like the old mongo documents: :id (str uid),
   :username (= login), :first_name (= name), :last_name (= surname),
   :about (= note); auth keys live in user_auth."
  (:require
    [clojure.string :as s]
    [tick.core :as t]
    [honeysql.helpers :as h]
    [next.jdbc :as next-jdbc]
    ;
    [sql.jdbc :as jdbc]
    [mlib.core :refer [to-int]]
    [sql.conn :refer [dbc]]
    [sql.core :refer [fetch fetch-one exec exec-on!]]
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
  (row->user
    (fetch-one
      (->
        SELECT_USER
        (h/merge-where [:= :uid (to-int uid)])))))


(defn users-by-ids [uids _flds]
  (when-let [uv (not-empty (vec uids))]
    (map row->user
      (fetch
        (->
          SELECT_USER
          (h/merge-where [:in :uid (map to-int uv)]))))))


(defn user-by-auth [auth flds]
  ;; auth key "un:login" / "em:email" -> user_auth -> users
  (when-let [a (not-empty (str auth))]
    (let [[t v] (s/split a #":" 2)]
      (when (and t (not-empty v))
        (->
          (fetch-one
            (-> (h/select :user_id)
                (h/from :user_auth)
                (h/where
                  [:= :auth_type t]
                  [:= :auth_id v])))
          (:user_id)
          (user-by-id flds))))))


(defn user-online? [id active-duration]
  (fetch-one
    (->
      (h/select :uid)
      (h/from :users)
      (h/merge-where
        [:= :uid (to-int id)]
        [:>= :atime (t/<< (t/date-time) active-duration)]))))


;;; ;;; ;;; ;;;

(defn new-uid []
  (with-open [conn (dbc)]
    (:nextval (jdbc/fetch-one conn ["select nextval('users_uid_seq')"]))))


(def FSET_COLS
  {:first_name :name
   :last_name :surname
   :about :note})


(defn user-update [uid fset]
  (= 1
    (exec
      (->
        (h/update :users)
        (h/sset
          (into {}
            (map (fn [[k v]] [(or (FSET_COLS k) k) v]))
            fset))
        (h/where [:= :uid (to-int uid)])))))


(defn- add-auth-key-on! [conn uid key-str]
  (let [[t v] (s/split key-str #":" 2)]
    (jdbc/execute conn
      [(str "insert into user_auth (auth_type, auth_id, user_id) values (?,?,?)"
            " on conflict (auth_type, auth_id) do nothing")
       t v (str uid)])))

(defn add-auth-key [uid key-str]
  (with-open [conn (dbc)]
    (add-auth-key-on! conn uid key-str)))


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
    (with-open [conn (dbc)]
      (next-jdbc/with-transaction [tx conn]
        (exec-on! tx {:insert-into :users :values [row]})
        (doseq [a (:auth data)]
          (add-auth-key-on! tx uid a))))
    (assoc data :id id)))


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
