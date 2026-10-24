
(ns sql.user
  (:require
    [clojure.string :as s]
    [honeysql.helpers :as h]
    [mlib.core :refer [to-int]]
    [sql.core :refer [fetch-one exec]]))
;


(def SELECT_USER
  (->
    (h/select :*)
    (h/from :users)
    (h/where [:= :%lower.email :?email])))
;


(defn active-user-by-email [eml]
  (when eml
    (fetch-one
      (h/merge-where SELECT_USER
        [:= :active :true] [:>= :uid 10])
      {:email (s/lower-case eml)})))
;


(defn sql-user-by-emlg [email-or-login]
  (when-let [emlg (-> email-or-login str not-empty)]
    (fetch-one
      (-> SELECT_USER
        (h/merge-select :password)
        (h/where
          [:or
            [:= :%lower.email :?emlg]
            [:= :%lower.login :?emlg]]))
      {:emlg (s/lower-case emlg)})))
;


;; temporary
(defn sql-user-by-uid [uid]
  (fetch-one
    (->
      (h/select :*)
      (h/from :users)
      (h/where [:= :uid (to-int uid)]))))
;


(defn create-sql-user [uid login email first_name last_name]
  (= 1
    (exec
      (->
        (h/insert-into :users)
        (h/values
          [{:active true
            :uid   (to-int uid)
            :login (str login)
            :email (str email)
            :name  (str first_name)
            :surname (str last_name)}])))))
;


(defn update-sql-user [uid first_name last_name town about]
  (->
    (h/update :users)
    (h/where [:= :uid uid])
    (h/sset
      { :name  (str first_name)
        :surname (str last_name)
        :town (str town)
        :note (str about)})
    (exec)
    (= 1)))
;

;;.
