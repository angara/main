(ns me.html
  (:require
    [mlib.core :refer [hesc]]
    [sql.user :refer [user-by-id]]
    [html.frame :refer [layout]]))


(defn tabs [{user :user} _curr]
  (let [userpage (str "https://angara.net/user/" (:username user) "/")]
    [:ul.nav.nav-tabs
     [:li {:role :presentation :class :active}
      [:a {:href "/me/"} "Информация"]]
     [:li {:role :presentation}
      [:a {:href "/mail/"} "Сообщения ...&raquo;&raquo;"]]
     [:li {:role :presentation}
      [:a {:href userpage} "Публичная страница ...&raquo;&raquo;"]]]))


(defn me-info [user]
  [:div.form-horizontal.b-me_info
   [:div.form-group
    [:label.control-label.col-sm-2 "E-mail:"]
    [:div.form-control-static (hesc (:email user))]]

   [:div.form-group
    [:label.control-label.col-sm-2 "Псевдоним:"]
    [:div.form-control-static (hesc (:username user))]]

   [:div.form-group {:data-fld "first_name"} ;.has-error
    [:label.control-label.col-sm-2 {:for "first_name"} "Имя:"]
    [:div.input-group.col-sm-8
     [:input#first_name.form-control
      {:type "text" :placeholder "Ваше настоящее имя"
       :value (:first_name user)}]
     [:span.input-group-btn
      [:button.btn.btn-default {:type :button} [:i.fa.fa-fw.fa-check]]]]]

   [:div.form-group {:data-fld "last_name"}
    [:label.control-label.col-sm-2 {:for "last_name"} "Фамилия:"]
    [:div.input-group.col-sm-8
     [:input#last_name.form-control
      {:type "text" :placeholder "Ваша фамилия"
       :value (:last_name user)}]
     [:span.input-group-btn
      [:button.btn.btn-default {:type :button} [:i.fa.fa-fw.fa-check]]]]]

   [:div.form-group  {:data-fld "town"}
    [:label.control-label.col-sm-2 {:for "town"} "Город:"]
    [:div.input-group.col-sm-8
     [:input#town.form-control
      {:type "text" :placeholder "Где живете"
       :value (:town user)}]
     [:span.input-group-btn
      [:button.btn.btn-default {:type :button} [:i.fa.fa-fw.fa-check]]]]]

   [:div.form-group {:data-fld "about"}
    [:label.control-label.col-sm-2 {:for "about"} "О себе:"]
    [:div.input-group.col-sm-8
     [:input#about.form-control
      {:type "text" :placeholder "Интересы, увлечения, контакты"
       :value (:about user)}]
     [:span.input-group-btn
      [:button.btn.btn-default {:type :button} [:i.fa.fa-fw.fa-check]]]]]
   [:div.clearfix]])


(defn home [req]
  (let [user (user-by-id (-> req :user :id)
                         [:email :username :first_name :last_name :town :about])]
    (layout
      req
      {:title "Личная информация"
       :js ["/incs/bootstrap-notify.min.js"
            "/incs/me/me-ui.js"]}
      ;
      [:div.b-me
       [:div.col-sm-10
        {:style "margin: 0 auto; float: none;"}
        [:div.row
         (tabs req nil)]

        [:br] [:br]
        (me-info user)
        [:br] [:br]

        [:div.row
         [:hr]
         [:button#btn_logout.btn.btn-warning "Выйти"
          [:span.glyphicon.glyphicon-log-out
           {:style "margin-left: 8px;"}]]]]])))
