(ns auth.reset
  (:require
    [taoensso.telemere :refer [log!]]
    [tick.core :as t]
    [mlib.core :refer [urand32]]
    [app.config :refer [conf]]
    [mlib.http :refer [json-resp]]
    [mlib.mail :refer [send-mail]]
    [mdb.sess :refer [clear-uid-sess]]
    [mdb.user :refer [user-update user-by-auth]]
    [auth.util :refer [em-norm migrate-user]]
    [auth.bcrypt :refer [hash-pass]]
    [sql.user :refer [active-user-by-email]]
    [html.frame :refer [layout]]))


;; TODO: implement
(def throttle (atom 0))


(defn reset-form [req]
  (layout req
          {:title "Установка пароля"
           :page-title "Установка пароля"
           :css ["/incs/styles.css"]
           :js  ["/incs/auth/reset.js"]}

          [:div.container
           [:div.b-auth.col-md-8.col-md-offset-2
            [:br]
            [:div.well
             "Чтобы переустановить пароль укажите свой регистрационный E-mail,"
             " получите письмо с проверочным кодом."
             " Введите полученный код и установите новый пароль,"
             " который будете использовать для входа на сайт."]

            [:form
             [:div.form-group
              [:label {:for "email"} "E-mail"]
              [:input#email.form-control
               {:type "text" :placeholder "Регистрационный Е-майл"}]]
             [:div.form-group
              [:button#get_code.btn.btn-primary {:type :submit}
               "Выслать код"]
              [:div#msg_code.text-center.msg "&nbsp;"]]]

            [:form
             [:div.form-group
              [:label {:for "code"} "Код"]
              [:input#code.form-control
               {:type "text" :placeholder "Проверочный код"}]]
             [:div.form-group
              [:label {:for "pass"} "Пароль"]
              [:input#pass.form-control
               {:type "text" :placeholder "Новый пароль, не менее 4 символов"}]]
             [:div.form-group
              [:button#set_pass.btn.btn-success {:type :submit}
               "Установить пароль"]
              [:div#msg_pass.text-center.msg "&nbsp;"]]]]]))
;


(defn send-code [uid email code]
  ;; TODO: throttle there
  (let [subj "Angara.Net - установка пароля"
        link (str (-> conf :urls :auth_reset) "?code=" code "&email=" email)
        text (str
              "\n"
              "Для установки пароля, перейдите по ссылке - " link "\n"
              "\n"
              "Проверочный код - " code
              "\n")]
    (log! ["auth/reset:" uid email code])
    (and
      (send-mail email subj text)
      (user-update uid {:passcode code :ts (t/date-time)}))))


(defn load-user [eml]
  (if-let [user (user-by-auth (em-norm eml) [:email])]
    user
    ;;
    (if-let [sql-user (active-user-by-email eml)]
      (migrate-user sql-user)
      nil)))


(defn get-code [{params :params}]
  (if-let [user (load-user (:email params))]
    (let [uid   (:id user)
          email (:email user)
          code  (str (quot (urand32) 1000))]
      (if (send-code uid email code)
        (json-resp
          {:ok 1
           :msg (str "Проверочный код выслан на E-mail: " email)})
        (json-resp
          {:err "smtp"
           :focus "#email"
           :msg "Ошибка при отправке проверочного кода."})))
    ;;
    (json-resp
      {:err "no_user"
       :focus "#email"
       :msg "Указанный E-mail отсутствует в базе или не активирован."})))


(defn set-pass [{{:keys [email code pass]} :params}]
  (if-let [user (user-by-auth (em-norm email) [:passcode :username])]
    (let [passcode (-> user :passcode not-empty)
          redir (-> conf :urls :login)
          uid (:id user)]
      (if (and passcode (= code passcode))
        (do
          (clear-uid-sess uid)
          (if (user-update uid
                           {:passhash (hash-pass pass) :passcode nil :ts (t/date-time)})
            (json-resp {:ok 1 :redir redir :msg "Пароль установлен."})
            (json-resp {:err "sys" :msg "Ошибка при установке пароля."})))
        (json-resp
          {:err "no_code" :msg "Неверный проверочный код."})))
    ;;
    (json-resp
      {:err "no_user"
       :msg "Пользователь отсутствует в базе или не активирован."})))
