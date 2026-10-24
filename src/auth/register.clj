
(ns auth.register
  (:require
    [clojure.string :as s]
    [mlib.core :refer [email? cap-first]]
    [mlib.http :refer [json-resp]]
    [auth.util :refer
     [norm-email norm-username em-norm un-norm
      norm-first_name norm-last_name]]
    [mdb.user :refer [user-by-auth new-uid user-create]]
    [sql.user :refer [sql-user-by-emlg create-sql-user]]))


(defn validate-email [params]
  (if-let [eml (-> params :email str s/trim email?)]
    (if
      (or
        (user-by-auth (em-norm eml) [:email])
        (sql-user-by-emlg (norm-email eml)))
      {::err "E-mail уже используется" :fld :email}
      {:email eml})
    {::err "Недопустимый формат поля E-mail" :fld :email}))


(defn validate-username [params]
  (let [username (-> params :username str s/trim)]
    (if-let [un (norm-username username)]
      (if
        (or
          (user-by-auth (un-norm username) [:username])
          (sql-user-by-emlg un))
        {::err "Псевдоним уже используется" :fld :username}
        {:username username})
      {::err "Неверные данные в поле Псевдоним" :fld :username})))


(defn validate-first_name [params]
  (if-let [nm (-> params :first_name str s/trim norm-first_name)]
    {:first_name nm}
    {::err "Неверные данные в поле Имя" :fld :first_name}))


(defn validate-last_name [params]
  (if-let [nm (-> params :last_name str s/trim norm-last_name)]
    {:last_name nm}
    {::err "Неверные данные в поле Фамилия" :fld :last_name}))


(defn validate-town [params]
  (let [town (-> params :town str s/trim)]
    (if (re-matches #"(?iu)[a-zа-яё0-9\-\.\,\ ]{2,60}" town)
      {:town (cap-first town)}
      {::err "Неверно введен Город" :fld :town})))


(defn register [{params :params :as req}]
  (let [data
        (reduce
          (fn [data validator]
            (let [res (validator params)]
              (if (::err res)
                (reduced res)
                (into data res))))
          {}
          [validate-email
           validate-username
           validate-first_name
           validate-last_name
           validate-town])]
    (if-let [errmsg (::err data)]
      (json-resp {:err :param :fld (:fld data) :msg errmsg})
      ;
      (if-let [user
               (user-create (new-uid)
                            (assoc data :auth [(em-norm (:email data))
                                               (un-norm (:username data))]))]
        (do
          (create-sql-user
            (:id user)
            (:username user)
            (:email user)
            (:first_name user)
            (:last_name  user))
          ;
          (json-resp
            {:ok 1
             :uid (:id user)
             :email (:email user)}))
        ;
        (json-resp {:err :db :msg "Ошибка при регистрации пользователя"})))))
;
