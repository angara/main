
(ns html.frame
  (:require
    [rum.server-render :refer [render-static-markup]]
    [mlib.conf :refer [conf]]
    [mlib.http :refer [make-url]]
    [mlib.web.snippets :refer [one-pix-src yandex-metrika mailru-top analytics]]
    [html.util :refer [ficon json-resp inner-html]]))
;

(defn login-url
  ([] "/user/login")
  ([next] (str "/user/login?next=" next)))
;


(def topmenu-items
  [
    {:id :main    :href "/"         :text "Главная"}      ;; Избранное
    {:id :events  :href "/events"   :text "События"}
    {:id :info    :href "/info"     :text "Информация"}   ;; Статьи
    ; {:id :maps    :href "/maps"     :text "Карты"}
    ; {:id :equip   :href "/equip"    :text "Снаряжение"}   ;; Магазины, Прокат
    {:id :touserv :href "/tourserv" :text "Турсервис"}
    {:id :meteo   :href "/meteo"    :text "Погода"}
    {:id :photo   :href "/photo"    :text "Фото"}
    {:id :forum   :href "/forum"    :text "Форум"}])
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


(def incs
  (list
    [:link
      { :rel "stylesheet" :type "text/css"
        :href "//api.angara.net/incs/uikit/2.27.2/css/uikit.gradient.min.css"}]
        ;; :href "//api.angara.net/incs/uikit/3.0.0/css/uikit.min.css"}]
        ; almost-flat

    ; [:link
    ;   { :rel "stylesheet" :type "text/css"
    ;     :href "//cdn.angara.net/libs/bootstrap/3.3.6/css/bootstrap.min.css"}]
    ; [:link
    ;   { :rel "stylesheet" :type "text/css"
    ;     :href "//cdn.angara.net/libs/bootstrap/3.3.6/css/bootstrap-theme.min.css"}]
    ; [:link
    ;   { :rel "stylesheet" :type "text/css"
    ;     :href "//cdn.angara.net/libs/font-awesome/4.6.3/css/font-awesome.min.css"}]

    [:script {:src "//api.angara.net/incs/jquery/3.1.1/jquery.min.js"}]
    [:script {:src "//api.angara.net/incs/uikit/2.27.2/js/uikit.min.js"}]))
    ;; [:script {:src "//api.angara.net/incs/uikit/3.0.0/js/uikit.min.js"}]))

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
      ;
      (og-meta-tags req wt og-meta)
      incs
      ;
      [:link {:rel "stylesheet" :type "text/css" :href "/incs/css/main.css"}]
      [:script {:src "/incs/js/mlib.js"}]
      [:script {:src "/incs/js/site.js"}]]))
;


(defn nav-topmenu [curr]
  [:nav.uk-navbar.b-navbar
    [:ul.uk-navbar-nav
      (for [mi topmenu-items]
        [:li (when (= curr (:id mi)) {:class "uk-active"})
          [:a.topmenu {:href (:href mi)} (:text mi)]])]])
;

(defn b-user [req user]
  [:.b-user
    (if user
      (let [nm (str (:name user) " " (:family user))
            login (:login user)
            attr {:href "/me/" :title (str login ": " nm)}]
        [:div.user
          ;; [:a attr [:img.upic {:src (:upic user one-pix-src)}]]
          [:a.name attr nm]
          [:a.login attr "@" login]])
      ;; not logged-in
      [:a.signin {:href (login-url (:uri req))}
        "Войти..." (ficon "sign-in marl-8")])])
;

(defn top-bar [req user curr]
  [:header.b-topbar
    [:div {:style {:padding "0 8px"}}
      [:.uk-grid.uk-grid-small
        [:.uk-width-medium-1-3
          [:.b-logo
            [:a {:href "//angara.net/"}
              [:img.logo {:src "/incs/img/angara_310.png" :alt "Angara.Net"}]]]]
        ;;
        [:.uk-width-medium-2-3.uk-grid.uk-grid-small
          [:.uk-width-medium-2-3.flex-mid
            [:.b-search.uk-form.uk-width-1-1
              [:input.search
                {:type 'text' :placeholder "Яндекс.Поиск по сайту ..."}]
              [:a.btn-search {:href "/yasearch"} (ficon "search")]]]
          ;;;
          [:.uk-width-medium-1-3.flex-mid (b-user req user)]]]
      ; top-grid
      (nav-topmenu curr)]])
;

(defn footer [req]
  [:footer.b-footer
    [:div.footer-bg
      [:div.uk-container.amar
        [:div.uk-grid.uk-clearfix
          [:div.uk-width-1-3.uk-text-left
             [:a {:href "http://angara.net/about/"} "О сайте"]
             [:br]
             [:a {:href "http://top.mail.ru/visits?id=474619" :target "_blank"}
                "Статистика"]]
          ;
          [:div.uk-width-1-3.uk-text-center]
          ;
          [:div.uk-width-1-3.uk-text-right.flex-bot
            [:div.copy
              "\u00A9 2002-2016 "
              [:a.brand {:href "http://angara.net/"} "Angara.Net"]
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
  [req {:keys [page-title page-nav topmenu] :as params} & content]
  (let [user (:user req)]
    (html5
      [:html
        (head req params)
        "\n"
        [:body
          [:div.page
            (when-let [uid (:uid user)]
              [:srcipt "window.uid='" uid "';"])

            (top-bar req user topmenu)

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
        [:.uk-panel-box
            {:style {:margin "20px"}}
          [:h2.uk-panel-title "Страница по этому адресу отсутствует."]
          [:br]
          [:p "Попробуйте воспользоваться "
            [:a {:href "/search"} "поиском"] "."]])})
;


(defn error-page [req msg]
  {:status 403
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body
      (layout req {}
        [:.uk-panel-box
          [:h1 msg]])})
;

;;.
