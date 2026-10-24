(ns auth.login
  (:require
    [taoensso.telemere :refer [log!]]
    [tick.core :as t]
    [app.config :refer [conf]]
    [mlib.web.sess :refer [sid-resp]]
    [mlib.http :refer [json-resp]]
    [auth.util :refer [req-ip migrate-user em-norm un-norm]]
    [auth.bcrypt :refer [check-pass]]
    [mdb.sess :refer [sess-new]]
    [mdb.user :refer [user-by-auth]]
    [sql.user :refer [sql-user-by-emlg]]))


(defn jerr [code msg]
  (json-resp {:err code :msg msg}))


(defn new-session [user tmp ip]
  (let [data {:uid (:id user) :ts (t/date-time) :ip ip}]
    (sess-new
      (if tmp
        (assoc data :tmp (int 1))
        data))))


(defn em-or-un [s]
  (or
    (em-norm s)
    (un-norm s)))


(defn load-user [eml pass]
  (if-let [user (user-by-auth (em-or-un eml) [:username :passhash])]
    (if (check-pass (str pass) (str (:passhash user)))
      user
      :wrong_pass)
    ;; sql-user
    (if-let [sql-user (sql-user-by-emlg eml)]
      (if (:active sql-user)
        (let [password (:password sql-user)]
          (if (and password (= password pass))
            (migrate-user sql-user)
            :wrong_pass))
        :inactive)
      :not_found)))


(defn do-login [{{:keys [email pass remember]} :params :as req}]
  (let [tmp (not remember)
        ip (req-ip req)
        user (load-user email pass)]
    (condp = user
      :not_found      (jerr :not_found "Пользователь не зарегистрирован.")
      :inactive       (jerr :inactive "Пользователь не активирован.")
      :wrong_pass     (jerr :wrong_pass "Неверный пароль.")
      ;
      (if (:id user)
        (let [sess (new-session user tmp ip)]
          (sid-resp
            (json-resp {:ok 1 :uid (:id user)})
            (:_id sess)
            (-> conf :auth :cookie-domain)
            tmp))
        ;
        (do
          (log! :warn ["unexpected load-user:" email])
          (jerr :syserr "Ошибка при загрузке данных пользователя."))))))
