
(ns html.frame
  (:require
    [rum.server-render :refer [render-static-markup]]
    ; [hiccup.core :refer [html]]
    ; [hiccup.page :refer [html5]]
    [mlib.conf :refer [conf]]
    [mlib.http :refer [make-url]]
    [mlib.web.snippets :refer [yandex-metrika mailru-top analytics]]
    [html.util :refer [ficon json-resp inner-html]]))
;

(defn login-url
  ([] "/user/login")
  ([next] (str "/user/login?next=" next)))
;


(defn og-meta-tags [req title og]
  (let [title (or (:title og) title "Angara.Net")
        descr (:descr og)
        img (str "http://angara.net"
              (or (:image og) "/incs/img/angara-og.png"))
        url (or (:url og)
              (make-url "http" "angara.net" (:uri req) (:query-string req)))]
    (list
      [:meta {:property "og:site_name" :content "Angara.Net"}]
      [:meta {:property "og:type"   :content "article"}]
      [:meta {:property "og:locale" :content "ru_RU"}]
      [:meta {:property "og:image"  :content img}]
      [:meta {:property "og:title"  :content title}]
      [:meta {:property "og:url"    :content url}]
      (when descr
        [:meta {:property "og:description" :content descr}]))))
;


(def cdn-libs
  (list
    [:link
      { :rel "stylesheet" :type "text/css"
        :href "//cdn.angara.net/libs/uikit/2.26.3/css/uikit.almost-flat.min.css"}]

    ; [:link
    ;   { :rel "stylesheet" :type "text/css"
    ;     :href "//cdn.angara.net/libs/bootstrap/3.3.6/css/bootstrap.min.css"}]
    ; [:link
    ;   { :rel "stylesheet" :type "text/css"
    ;     :href "//cdn.angara.net/libs/bootstrap/3.3.6/css/bootstrap-theme.min.css"}]
    ; [:link
    ;   { :rel "stylesheet" :type "text/css"
    ;     :href "//cdn.angara.net/libs/font-awesome/4.6.3/css/font-awesome.min.css"}]

    [:script {:src "//cdn.angara.net/libs/jquery/3.0.0/jquery.min.js"}]
    [:script {:src "//cdn.angara.net/libs/uikit/2.26.3/js/uikit.min.js"}]))

    ; [:script
    ;   { :src "//cdn.angara.net/libs/bootstrap/3.3.6/js/bootstrap.min.js"
    ;     :defer 1}]))
;

(defn head [req {:keys [title page-title js css og-meta]}]
  (let [wt (or title page-title)]
    [:head
      [:title "Angara.Net" (and wt (str ": " wt))]
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
      [:link {:rel "shortcut icon" :href "/incs/img/favicon.ico"}]
      ;; [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
      ;
      (og-meta-tags req wt og-meta)
      cdn-libs
      ;
      [:link {:rel "stylesheet" :type "text/css" :href "/incs/css/main.css"}]
      [:script {:src "/incs/js/mlib.js"}]]))
;

(defn top-bar [req user]
  [:header.b-topbar
    [:.uk-container.uk-clearfix.amar
      ;
      [:a {:href "//angara.net/"}
        [:img.logo  {:src "/incs/img/angara_310.png" :alt "Angara.Net"}]]

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
              "Войти" (ficon "sign-in marl-8")]])]]
        ;
    [:div.clearfix]])
;

(defn footer [req]
  [:footer.b-footer
    [:div.footer-bg
      [:.uk-container.amar
        [:div.uk-grid.uk-clearfix
          [:div.uk-width-1-3.uk-text-left
             [:a {:href "http://angara.net/about/"} "О сайте"]
             [:br]
             [:a {:href "http://top.mail.ru/visits?id=474619" :target "_blank"}
                "Статистика"]]
          ;
          [:div.uk-width-1-3.uk-text-center]
          ;
          [:div.uk-width-1-3.uk-text-right.flex-bottom
            [:div.copy
              "\u00A9 2002-2016 "
              [:a.copy-tm {:href "http://angara.net/"} "Angara.Net"]
              "\u2122"]]
          ;
          [:div.clearfix]]]]

    ;; counters
    (inner-html (yandex-metrika (:yandex-metrika conf)))
    (inner-html (mailru-top (:mailru-top conf)))
    (inner-html (analytics (:analytics conf)))])
;


(defn html5 [content]
  (str "<!DOCTYPE html>\n"
    (render-static-markup content)))


(defn html-resp [s]
  {:status 200
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body s})


(defn layout
  [req {:keys [page-title page-nav] :as params} & content]
  (let [user (:user req)]
    (html5
      [:html
        (head req params)
        "\n"
        [:body
          [:div.page
            (when-let [uid (:uid user)]
              [:srcipt "window.uid='" uid "';"])

            (top-bar req user)

            [:div.content
              [:div.uk-container.amar
                page-nav
                (when page-title
                  [:h1.page-title page-title])
                content
                [:.uk-clearfix]
                [:.b-botnav
                  [:a {:href "http://angara.net/"} "Главная"]
                  " | "
                  [:a {:href "/bb/"} "Объявления"]
                  " | "
                  [:a {:href "/meteo/"} "Погода"]
                  " | "
                  [:a {:href "/forum/"} "Форум"]]]]]

          (footer req)]])))
      ; /html
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
          [:p "Попробуйте воспользоваться "
            [:a {:href "/search"} "поиском"] "."]])})
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
