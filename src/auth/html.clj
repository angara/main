(ns auth.html
  (:require
    [app.config :refer [conf]]
    [html.frame :refer [layout]]))


(defn login-form [req]
  (let [redir (-> req :params :redir not-empty)]
    (layout
      (assoc req :user false)
      {:title "Вход на сайт"
       :page-title "Вход на сайт"
       :js  ["/incs/auth/login.js"]}

      [:div.container
       [:div.b-login.col-md-6.col-md-offset-3
        [:br]
        [:form
         [:div.form-group
          [:label {:for "email"} "E-mail"]
          [:input#email.form-control
           {:type "text" :placeholder "Электронная почта или псевдоним"}]]
         [:div.form-group
          [:label {:for "pass"} "Пароль"]
          [:input#pass.form-control
           {:type "password" :placeholder ""}]]
         [:div.form-group.checkbox
          [:label {:for "remember"}
           [:input#remember {:type "checkbox" :checked true}]
           " Запомнить"]]
         [:div.form-group.row
          [:div.col-sm-5
           {:style "margin-top: 14px;"}
           [:button#login.btn.btn-primary.btn-block
            {:type "submit"}
            "Войти на сайт"]]
          [:div.col-sm-5.pull-right
           {:style "margin-top: 14px;"}
           [:a.btn.btn-default.btn-block
            {:href (-> conf :urls :auth_reset)}
            "Восстановить пароль"]]
          [:div.clearfix]
          [:div#msg.msg.msg_err.text-center
           {:style "margin-top: 8px;"}
           "&nbsp;"]
          [:div.text-center
           {:style "font-size: 120%; margin-top: 8px;"}
           [:a {:href (-> conf :urls :register)} "Регистрация ..."]]]]
        [:div.clearfix]]])))


(defn register-form [req]
  (layout
    (assoc req :user false)
    {:title "Регистрация"
     :page-title "Регистрация"
     :js ["/incs/auth/register.js"]}

    [:div.container
     [:div.b-register.col-md-6.col-md-offset-3
      [:br]
      [:form
       [:div.form-group
        [:label {:for "email"} "E-mail"]
        [:input#email.form-control
         {:type "text" :placeholder "Электронная почта"}]]
       [:div.form-group
        [:label {:for "username"} "Псевдоним"]
        [:input#username.form-control
         {:type "text"
          :placeholder "Латинские буквы и цифры, не менее 4-х, начинается с буквы"}]]
       [:div.form-group
        [:label {:for "first_name"} "Имя"]
        [:input#first_name.form-control
         {:type "text" :placeholder "Ваше настоящее имя, не менее 2-х букв"}]]
       [:div.form-group
        [:label {:for "last_name"} "Фамилия"]
        [:input#last_name.form-control
         {:type "text" :placeholder "Ваша фамилия, не менее 2-х букв"}]]
       [:div.form-group
        [:label {:for "town"} "Город"]
        [:input#town.form-control
         {:type "text" :placeholder "Где живете в настоящее время"}]]
       [:div.form-group
        {:style "margin-top: 22px;"}
        [:button#btn_register.btn.btn-primary
         {:type "submit"}
         "Зарегистрироваться"]]
       [:div#msg.msg.msg_err.text-center
        {:style "margin-top: 8px;"}
        "&nbsp;"]
       [:div.clearfix]]]]))
