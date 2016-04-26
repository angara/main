
(ns html.views
  (:require
    [html.frame :refer [layout]]))
;

(defn main-page [req]
  (layout req
    {}
    [:div.jumbotron
      [:h1.text-center "Раздел в разработке."]
      [:p
        [:a {:href "/bb/"} "Доска объявлений"]]]))
;

(defn home [req]
  (layout req
    {:title "Личная информация"}
    ;
    [:div {:style "margin: 20px;"}
      [:a {:href (str "http://angara.net/user/" (:login (:user req)) "/")}
          "Личная страница пользователя сайта Angara.Net"]]
    [:hr]
    [:button#btn_logout.btn "Выйти"
      [:span.glyphicon.glyphicon-log-out {:style "margin-left: 8px;"}]]
    [:script
     "
$('#btn_logout').click(function(){
    lib.post('/me/logout', {}, function(resp){
        if(resp.redir){ window.location.href = resp.redir; }
    });
});"]))

;;.
