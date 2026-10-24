(ns me.core
  (:require
    [clojure.string :as s]
    [compojure.core :refer [GET POST defroutes]]
    [taoensso.telemere :refer [log!]]
    [mlib.core :refer [str-head cap-first to-int]]
    [mlib.http :refer [json-resp]]
    [mdb.user :refer [user-by-id user-update]]
    [sql.user :refer [update-sql-user]]
    [auth.util :refer [norm-first_name norm-last_name]]
    [me.html :refer [home]]))


(defn validate-first_name [val]
  (if-let [nm (-> val str s/trim norm-first_name)]
    {:first_name nm}
    {::err "Неверные данные в поле Имя" :fld :first_name}))


(defn validate-last_name [val]
  (if-let [nm (-> val str s/trim norm-last_name)]
    {:last_name nm}
    {::err "Неверные данные в поле Фамилия" :fld :last_name}))


(defn validate-town [val]
  (let [town (-> val str s/trim)]
    (if (re-matches #"(?iu)[a-zа-яё0-9\-\.\,\ ]{2,60}" town)
      {:town (cap-first town)}
      {::err "Неверно введен Город" :fld :town})))


(defn validate-about [val]
  (let [val (-> val str s/trim)]
    {:about (str-head 1000 val)}))


(def validator-map
  {:first_name validate-first_name
   :last_name  validate-last_name
   :town  validate-town
   :about validate-about})


(defn update-user [user data]
  (and
    (user-update
      (:id user) data)
    (update-sql-user
      (to-int (:id user))
      (get data :first_name (:first_name user))
      (get data :last_name  (:last_name  user))
      (get data :town  (:town  user))
      (get data :about (:about user)))))


(defn update-field [req]
  (let [params (:params req)
        fld (keyword (:fld params))
        val (:val params)]
    (if-let [validator (get validator-map fld)]
      (let [res (validator val)]
        (if-let [msg (::err res)]
          (json-resp {:err :invalid :msg msg :fld fld})
          (if (update-user
                (user-by-id (-> req :user :id)
                            [:email :username :first_name :last_name :town :about])
                res)
            (json-resp {:ok 1 :fld fld})
            (json-resp {:err :db :msg "Ошибка при записи в базу данных."}))))
      (do
        (log! :warn ["me/update - unexpected fld:" fld])
        (json-resp {:err :param})))))
;

;; wrap-user-required
(defroutes me-routes
  (GET  "/"       [] home)
  (POST "/update" [] update-field))
;
