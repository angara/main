
(ns misc.icestorm
  (:require
    [clojure.string :as s]
    [mlib.log :refer [warn]]
    [compojure.core :refer [GET POST defroutes]]
    [postal.core :refer [send-message]]
    [mlib.conf :refer [conf]]
    [mlib.core :refer [to-int]]
    [mlib.http :refer [json-resp]]
    [html.frame :refer [layout]]))
;


(defn index [req]
  (layout req
    {:title "IceStorm - 2019: регистрация"
     :page-title "Ледовый Шторм 2019 - Ice Storm 2019"}
    [:div.icestorm
      [:div.container
        {:style {:padding "14px 0 20px 0"}}

        [:div.col-md-8.col-md-offset-2
          [:h2.text-center "Регистрация / Registration"]

          [:script {:src "/incs/misc/icestorm.js"}]

          ; [:div {:style "margin: 4em; text-align: center;"}
          ;   [:h4 "Регистрация завершена."]]]]]))

          [:div#form.form

            [:div.form-group
              [:label {:for "name"} "Ф.И.О. / Full name:"]
              [:div
                [:input#name.form-control {:type "text"}]]]
            ;
            [:div.form-group
              [:label {:for "town"} "Город / Country, City:"]
              [:div
                [:input#town.form-control {:type "text" :style "width: 30em;"}]]]
            ;
            [:div.form-group
              [:label {:for "email"} "E-mail:"]
              [:div
                [:input#email.form-control {:type "text" :style "width: 30em;"}]]]
            ;
            [:div.form-group
              [:label {:for "phone"} "Телефон / Phone:"]
              [:div
                [:input#phone.form-control {:type "text" :style "width: 20em;"}]]]
            ;
            [:div.form-group
              [:label {:for "age"} "Возраст / Age:"]
              [:div
                [:input#age.form-control
                  { :type "text"
                    :style "text-align: center; width: 4em;"}]]]
            ;
            [:div.form-group
              [:label {:for "disc"} "Дисциплина / Sport:"]
              [:div
                [:select#disc.form-control {:style "width: 14em;"}
                  [:option {:value ""}]
                  [:option {:value "Велосипед"} "Велосипед / Bicycle"]
                  [:option {:value "Коньки"} "Коньки / Skating"]
                  [:option {:value "Лыжи"} "Лыжи / Skiing"]]]
              [:div.clearfix]]
            ;
            [:div.form-group
              [:label ""]
              [:button#send.btn.btn-success
                [:b "Подать заявку / Submit"]]]]]]]))
;


(def RE_NAME
  #"(?ui)^[abcdefghijklmnopqrstuvwxyzабвгдеёжзийклмнопрстуфхцчшщъыьэюя\.\-\ ]{5,80}$")
;

(def RE_PHONE
  #"^[0-9\+\(\)\,\.\- ]{6,40}$")
;

(def RE_EMAIL
  #"(?i)^[0-9a-z\.\-\_]+@[0-9a-z\.\-]+[a-z][a-z]$")
;

(defn validate-name [s]
  (when (re-matches RE_NAME s) s))
;

(defn validate-phone [p]
  (when (re-matches RE_PHONE p) p))
;

(defn validate-email [e]
  (let [l (.length e)]
    (when
      (and (<= 6 l) (>= 80 l)) (re-matches RE_EMAIL e)
      e)))
;

;       # ip = req.ips[req.ips.length-1]
;       # ua = req.headers['user-agent']
;       # now = new Date()
;       # #
;       # last_ip_add = ip_lim[ip]
;       # if last_ip_add?.ts.getTime() > now.getTime() - 3*60*1000
;       #     return res.send {err:"limit",msg:"Повторите запрос позже."}
;       # #


(defn send-text [text]
  (try
    (let [smtp  (-> conf :icestorm :smtp)
          envel (-> conf :icestorm :envel)
          rc  (send-message smtp
                (merge envel {:type "text/plain;charset=utf-8" :body text}))]
      (-> rc :error (= :SUCCESS)))
    (catch Exception e 
      (warn "send-text:" e))))
;

(defn register [{params :params :as req}]
  (let [name  (-> params :name  str s/trim validate-name)
        phone (-> params :phone str s/trim validate-phone)
        email (-> params :email str s/trim validate-email)
        town  (-> params :town  str s/trim not-empty)
        age   (-> params :age   (to-int 0))
        disc  (-> params :disc  str s/trim not-empty)]
    ;
    (json-resp
      (cond
        (not name)
        {:err :invalid :fld :name :msg "Неверно введено Ф.И.О."}

        (not town)
        {:err :invalid :fld :town :msg "Не указан город."}

        (and (not phone) (not email))
        {:err :invalid :fld :email :msg "Не указан телефон или e-mail."}

        (or (< age 10) (> age 100))
        {:err :invalid :fld :age :msg "Недопустимый возраст."}

        (not disc)
        {:err :invalid :fld :disc :msg "Не указана дисциплина."}

        :else
        (if
          (send-text
            (str
              "\n"
              "Ф.И.О. участника: " name  "\n"
              "Город:            " town  "\n"
              "E-mail:           " email "\n"
              "Телефон:          " phone "\n"
              "Возраст:          " age   "\n"
              "Дисциплина:       " disc  "\n"
              "\n"))
          {:ok 1}
          {:err :smtp :msg "Ошибка при отправке заявки!"})))))
;

;              from: "noreply@angara.net"
;              to: "icestorm@angara.net"
;              subject: "IceStorm: регистрация участника"
;
;  #-




(defroutes routes
  (GET  "/register" [] index)
  (POST "/register" [] register))
;

;;.
