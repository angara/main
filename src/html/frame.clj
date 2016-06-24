
(ns html.frame
  (:require
    [rum..server-render :refer [render-static-markup]]
    [hiccup.core :refer [html]]
    [hiccup.page :refer [html5]]
    [mlib.conf :refer [conf]]
    [mlib.http :refer [make-url]]
    [mlib.web.snippets :refer [yandex-metrika mailru-top]]
    [html.util :refer [inc-pfx inc-js inc-css glyphicon json-resp]]))
;

(defn login-url
  ([] "/user/login")
  ([next] (str "/user/login?next=" next)))
;

(defn head
  [req {:keys [title page-title js css og-title og-image og-url og-descr]}]
  (let [wt (or title page-title)
        og-tit (or og-title wt "Angara.Net")
        og-img (str "http://angara.net" (or og-image (inc-pfx "img/angara-og.png")))
        og-url (or og-url
                (make-url "http" "angara.net" (:uri req) (:query-string req)))]
    [:head
      [:title "Angara.Net" (and wt (str ": " wt))]
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
      [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
      [:link {:rel "shortcut icon" :href (inc-pfx "img/favicon.ico")}]
      [:meta {:property "og:site_name" :content "Angara.Net"}]
      [:meta {:property "og:type"   :content "article"}]
      [:meta {:property "og:locale" :content "ru_RU"}]
      [:meta {:property "og:image"  :content og-img}]
      [:meta {:property "og:title"  :content og-tit}]
      [:meta {:property "og:url"    :content og-url}]
      (when og-descr [:meta {:property "og:description" :content og-descr}])
      ; css
      [:link {:rel "stylesheet" :type "text/css"
              :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"}]
      [:link {:rel "stylesheet" :type "text/css"
              :href "https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css"}]
      (map inc-css (concat ["main.css"] css))
      ; js
      [:script {:type "text/javascript"
                :src  "https://code.jquery.com/jquery-1.12.1.min.js"}]
      [:script {:type "text/javascript" :defer 1
                :src  "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"}]
      (map inc-js (concat ["mlib.js" "site.js"] js))]))
;

(defn top-bar [req user]
  [:div.b-topbar
    [:div.container
      ;
      [:a {:href "//angara.net/"}
        [:img#toplogo.logo
          {:src (inc-pfx "img/angara_310.png") :alt "Angara.Net"}]]

; [:li [:a {:href "/usr/fav/" :title "Избранное"}
;     [:span.glyphicon.glyphicon-star] ]]
; [:li [:a {:href "#" :title "События"}
;     [:span.glyphicon.glyphicon-calendar] ]]
; [:li [:a {:href "/meteo/" :title "Погода"}
;     [:span.glyphicon.glyphicon-cloud] ]]
; [:li [:a {:href "#" :title "Фото"}
;     [:span.glyphicon.glyphicon-picture] ]]
; [:li [:a {:href "/search/" :title "Поиск"}
;     [:span.glyphicon.glyphicon-search] ]]

; [:li.dropdown
;     [:a.dropdown-toggle {
;             :href "#" :data-toggle "dropdown" :role "button"
;         }
;         "Поиск" [:span.caret]
;     ]
;     [:ul.dropdown-menu {:role "menu"}
;         [:li [:a {:href "#"} "По тэгу"]
;             [:input.form-control]
;             [:button.btn.btn-primary.btn-sm "По тэгам"]
;         ]
;         [:li.divider]
;         ; [:li.dropdown-header "nav header"]
;         [:li [:a {:href "#"} "Яндекс"]]
;         [:li [:a {:href "#"} "Google"]]
;     ]
; ]
      [:div.col-sm-3.pull-right
        (if user
          (let [nm (str (:name user) " " (:family user))]
            [:div.user
                [:a {:href "/me/" :title (str (:login user) ": " nm)}
                  [:b (:login user)] [:br] nm]])
          ;; no logged in
          [:div.signin
            [:a.btn.btn-default {:href (login-url (:uri req))}
              "Войти" (glyphicon "log-in marl-8")]])]]
        ;
    [:div.clearfix]])
;

(defn footer [req]
  [:div.b-footer
    [:div.footer-bg
      [:div.container
        [:div.row
          [:div.col-sm-4.text-left
             [:a {:href "http://angara.net/about/"} "О сайте"]
             [:br]
             [:a {:href "http://top.mail.ru/visits?id=474619" :target "_blank"}
                 "Статистика"]]
          ;
          [:div.col-sm-4.text-center]
          ;
          [:div.col-sm-4.text-right
            [:div.copy
              [:a.copy-tm {:href "http://angara.net/"} "Angara.Net"]
              " &copy; 2002-2016"]]
          ;
          [:div.clearfix]]]]
    ;; counters
    (yandex-metrika (:yandex-metrika conf))
    (mailru-top (:mailru-top conf))])
;


(defn render [content]
  (str "<!DOCTYPE html>\n"
    (render-static-markup content)))


(defn layout
  [req {:keys [page-title page-nav] :as params} & content]
  (let [user (:user req)]
    (render
      [:html
        (head req params)
        "\n"
        [:body
          [:div.page
            (when-let [uid (:uid user)]
              [:srcipt "window.uid='" uid "';"])
            ;
            (top-bar req user)
            ;
            [:div.content
              [:div.container
                page-nav
                (when page-title
                  [:h1.page-title page-title])
                content
                [:div.clearfix]
                [:div.b-botnav
                  [:a {:href "http://angara.net/"} "Главная"]
                  " | "
                  [:a {:href "/bb/"} "Объявления"]
                  " | "
                  [:a {:href "/meteo/"} "Погода"]
                  " | "
                  [:a {:href "/forum/"} "Форум"]]]]
            ;
            (footer req)]]])))
    ; /html5
;


(defn no-access [req]
  {:status 403
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body
      (layout req {:title "Нет доступа"}
        [:div.jumbotron
          [:h1 "У вас нет доступа к этой странице"]
          [:p  "Проверьте правильность входа на сайт."]])})
;


(defn not-found [req]
  {:status 404
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body
      (layout req {:title "Страница не найдена"}
        [:div.jumbotron
          [:h2 "Страница по этому адресу отсутствует."]
          [:br]
          [:p "Попробуйте воспользоваться <a href='/search'>поиском</a>."]])})
;


(defn error-page [req msg]
  {:status 403
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body
      (layout req {}
        [:div.jumbotron.error
          [:h1 msg]])})
;


;;.
