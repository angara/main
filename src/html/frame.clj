
(ns html.frame
  (:require
    [hiccup.page :refer [html5]]
    [mlib.conf :refer [conf]]
    [mlib.http :refer [make-url]]
    [mlib.web.snippets :refer [yandex-metrika mailru-top]]
    [html.util :refer [glyphicon css-link script]]))
;


(def INCS "/incs/")

(defn inc-pfx [uri]
  (if (= (first uri) \/) (str uri) (str INCS uri)))
;

(defn login-url [& [redir]]
  (let [url (-> conf :urls :login)]
    (if redir
      (str url "?redir=" redir)
      url)))
;


(defn head-meta
  [req {:keys [title page-title og-title og-image og-url og-descr]}]
  (let [wt (or title page-title)
        og-tit (or og-title wt "Angara.Net")
        og-img (str "http://angara.net" (or og-image (inc-pfx "img/angara-og.png")))
        og-url (or og-url
                (make-url "http" "angara.net" (:uri req) (:query-string req)))]
    (list
      [:title "Angara.Net" (and wt (str ": " wt))]
      [:meta {:charset "utf-8"}]
      [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
      [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
      [:meta {:property "og:site_name" :content "Angara.Net"}]
      [:meta {:property "og:type"   :content "article"}]
      [:meta {:property "og:locale" :content "ru_RU"}]
      [:meta {:property "og:image"  :content og-img}]
      [:meta {:property "og:title"  :content og-tit}]
      [:meta {:property "og:url"    :content og-url}]
      (when og-descr
        [:meta {:property "og:description" :content og-descr}])
      [:link {:rel "shortcut icon" :href "//angara.net/favicon.ico"}])))
;


(def bootstrap-head
  (list
    [:link {:rel "stylesheet" :type "text/css"
            :href "//api.angara.net/incs/bootstrap/3.3.7/css/bootstrap.min.css"}]
    [:link {:rel "stylesheet" :type "text/css"
            :href "//api.angara.net/incs/font-awesome/4.7.0/css/font-awesome.min.css"}]
    [:script {:type "text/javascript"
              :src  "//api.angara.net/incs/jquery/3.2.0/jquery.min.js"}]
    [:script {:type "text/javascript" :defer 1
              :src  "//api.angara.net/incs/bootstrap/3.3.7/js/bootstrap.min.js"}]))
;

;;; ;;; ;;; ;;; ;;;

(defn user-block [req]
  (let [user (:user req)]
    (if user
      (let [nm (str (:first_name user) " " (:last_name user))]
        [:div.user
          [:a {:href "/me/" :title (str (:username user) ": " nm)}
            [:b (:username user)] [:br] nm]])
      ;; no logged in
      (when (not= false user)
        [:div.signin
          [:a.btn.btn-default {:href (login-url (:uri req))}
            "Войти" (glyphicon "log-in marl-8")]]))))
;

(defn top-bar [req]
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
        (user-block req)]]
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
              [:br]
              [:a.copy-tm {:href "http://angara.net/"} "Angara.Net"]
              " &copy; 2002-2017"]]
          ;
          [:div.clearfix]]]]
    ;; counters
    (yandex-metrika (:yandex-metrika conf))
    (mailru-top (:mailru-top conf))])
;

(defn head [req {:keys [css js] :as params}]
  [:head
    (head-meta req params)
    bootstrap-head
    ;
    (map #(css-link (inc-pfx %))
      (concat ["css/main.css"] css))
    (map #(script (inc-pfx %))
      (concat ["js/mlib.js" "js/site.js"] js))])
;


(defn layout
  [ req
    {:keys [page-title page-nav] :as params}
    &
    content]
  ;
  (let [user (:user req)]
    (html5
      (head req params)
      "\n"
      [:body
        [:div.page
          ;; NOTE: token specified by cookie in middleware
          ;; (when csrf [:srcipt "window._csrf='" csrf "';"])
          (when-let [uid (:id user)]
            [:script "window.uid='" uid "';"])
          ;
          (top-bar req)
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
          (footer req)]])))
    ; /html5
;


(defn render [req params & content]
  {:status 200
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body (apply layout req params content)})
;

(defn no-access [req]
  {:status 403
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body
      (layout req {:title "Нет доступа"}
        [:div.jumbotron
          [:h1 "У вас нет доступа к этой странице"]
          [:p  "Проверьте правильность входа на сайт."]])})
;


(defn not-found [req]
  {:status 404
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body
      (layout req {:title "Страница не найдена"}
        [:div.jumbotron
          [:h2 "Страница по этому адресу отсутствует."]
          [:br]
          [:p "Попробуйте воспользоваться <a href='/search'>поиском</a>."]])})
;


(defn error-page [req msg]
  {:status 403
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body
      (layout req {}
        [:div.jumbotron.error
          [:h1 msg]])})
;

;;.
