(ns html.frame
  (:require
    [hiccup.page :refer [html5]]
    [app.config :refer [conf]]
    [mlib.http :refer [make-url]]
    [mlib.web.snippets :refer [yandex-metrika mailru-top ya-rtb]]
    [html.search :refer [ya-site-form]]
    [meteo.curr-temp :refer [curr-temp]]
   ,))


(defn script [js-file]
  [:script {:src js-file :type "text/javascript" :defer 1}])


(defn css-link [css-file]
  [:link {:href css-file :rel "stylesheet" :type "text/css"}])


(defn glyphicon [nm]
  [:span {:class (str "glyphicon glyphicon-" nm)}])


(def INCS "/incs/")

(defn inc-pfx [uri]
  (if (= (first uri) \/) (str uri) (str INCS uri)))


(defn login-url [& [redir]]
  (let [url (-> conf :urls :login)]
    (if redir
      (str url "?redir=" redir)
      url)))


(def DESCR
  (str "Активный отдых, спорт, туризм, путешествия, фото."
      " Новости экстремального спорта, календарь событий, фотографии."
      " Байкал и Прибайкалье. Прогноз погоды."))


(defn head-meta
  [req {:keys [title page-title og-title og-image og-url og-descr]}]
  (let [wt (or title page-title)
        ds (or og-descr DESCR)
        og-tit (or og-title wt "Angara.Net")
        og-img (str "https://angara.net" (or og-image (inc-pfx "img/angara-og.png")))
        og-url (or og-url
                (make-url "https" "angara.net" (:uri req) (:query-string req)))]
    (list
      [:title "Angara.Net" (and wt (str ": " wt))]
      [:meta {:charset "utf-8"}]
      [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
      [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
      [:meta {:name "description" :content ds}]
      [:meta {:property "og:site_name" :content "Angara.Net"}]
      [:meta {:property "og:type"   :content "article"}]
      [:meta {:property "og:locale" :content "ru_RU"}]
      [:meta {:property "og:image"  :content og-img}]
      [:meta {:property "og:title"  :content og-tit}]
      [:meta {:property "og:url"    :content og-url}]
      [:meta {:property "og:description" :content ds}]
      [:link {:rel "shortcut icon" :href "//angara.net/favicon.ico"}])))


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

;;; ;;; ;;; ;;; ;;;

(def user_popmenu_items
  (str
    "["
      "[\"/me/\", \"Профиль\"],"
      "[\"/mail/\", \"Сообщения\"],"
      "[\"\", \"-\"],"
      "[do_logout, \"Выйти\"]"
    "]"))


(defn user-block [user uri]
  (if user
    (let [nm (str (:first_name user) " " (:last_name user))]
      (list
        [:div#topbar_user.b-user.c-popmenu
          [:div.c-popmenu-toggle
            {:style "float:right; margin-top: 10px;"}
            [:i.fa.fa-fw.fa-caret-down]]
          [:div.name
            [:a {:href "/me/" :title (str (:username user) ": " nm)}
              [:b (:username user)] [:br] nm]]]
        [:script
          "$(function(){"
            "$('#topbar_user')"
              ".data('popmenu'," user_popmenu_items ");"
          "});"]))
    ;; no logged in
    (when (not= false user)
      [:div.b-signin
        [:a.btn.btn-default {:href (login-url uri)}
          "Войти" (glyphicon "log-in marl-8")]])))


(defn topbar [req]
  (let [user (:user req)]
    [:div.b-topbar
      [:div.container
        [:div.row
          [:div.col-md-4
            [:a {:href "//angara.net/"}
              [:img.logo
                {:src (inc-pfx "img/angara_310.png") :alt "Angara.Net"}]]]
          ;
          (when (not= false user)
            [:div.col-md-5
              [:div.b-search
                (let [qp (:query-params req)
                      text (and (get qp "searchid") (get qp "text"))]
                  (ya-site-form text))]])
          ;
          [:div.col-md-3.pull-right
            (user-block user (:uri req))]
          ;
          [:div.clearfix]]]]))


(def TOP_NAVS
  [
    {:id "main"     :href "/"           :menu "Главная"}
    {:id "calendar" :href "/calendar"   :menu "Календарь"}
    {:id "info"     :href "/info/"      :menu "Информация"}
    ;; Карты
    {:id "text"     :href "/publ/"      :menu "Статьи"}
    {:id "tourserv" :href "/tourserv"   :menu "Турсервис"}
    ;; {:id "photo"    :href "/photo/"     :menu "Фото"}
    {:id "forum"    :href "/forum/"     :menu "Форум"}
    {:id "meteo"    :href "/meteo/"     :menu "Погода"  :ext curr-temp}])


(defn topnav [req active]
  (let [act (and active (name active))]
    [:div.b-topnav
      [:div.container
        [:ul.menu
          (for [n TOP_NAVS :let [ext-fn (:ext n)]]
            [:li {:class (when (= act (:id n)) "active")}
              [:a {:href (:href n)} (:menu n)]
              (when ext-fn
                (ext-fn req))])]]]))


(defn footer [_req]
  [:div.b-footer
    [:div.footer-bg
      [:div.container
        [:div.row
          [:div.col-sm-4.text-left
             [:a {:href "//angara.net/about/"} "О сайте"]
             [:br]
             [:a {:href "http://top.mail.ru/visits?id=474619" :target "_blank"}
                 "Статистика"]]
          ;
          [:div.col-sm-4.text-center]
          ;
          [:div.col-sm-4.text-right
            [:div.copy
              [:br]
              [:a.copy-tm {:href "https://angara.net/"} "Angara.Net"]
              " &copy; 2002-2025"]]
          ;
          [:div.clearfix]]]]
    ;; counters
    (yandex-metrika (:yandex-metrika conf))
    (mailru-top (:mailru-top conf))])


(defn head [req {:keys [css js] :as params}]
  [:head
    (head-meta req params)
    bootstrap-head
    ;
    (map #(css-link (inc-pfx %))
      (concat ["css/main.css"] css))
    (map #(script (inc-pfx %))
      (concat ["js/mlib.js" "js/site.js"] js))])


(defn layout
  [ req
    {:keys [page-title page-nav topmenu rtb-top rtb-bottom] :as params}
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
          (topbar req)
          (topnav req topmenu)
          ;
          [:div.content
            [:div.container
              page-nav
              ;
              (when rtb-top
                [:div.rtb-top {:style "overflow: auto; display: block;"}
                  (ya-rtb rtb-top true) 
                  [:div.clearfix]])
              ;
              (when page-title
                [:h1.page-title page-title])
              ;
              content
              [:div.clearfix]
              ;
              [:div.b-botnav
                [:a {:href "//angara.net/"} "Главная"]
                (for [n (next TOP_NAVS)]
                  (list
                    " | "
                    [:a {:href (:href n)} (:menu n)]))]
              ;
              (when rtb-bottom
                [:div.rtb-bottom {:style "overflow: auto; display: block;"}
                  (ya-rtb rtb-bottom true)
                  [:div.clearfix]])]]
          ;
          (footer req)]])))
    ; /html5


(defn html5-resp [content]
  {:status 200
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body (html5 content)})


(defn render-layout [req params & content]
  {:status 200
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body (apply layout req params content)})


(defn no-access [req]
  {:status 403
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body
      (layout req {:title "Нет доступа"}
        [:br]
        [:div.jumbotron
          [:h2.text-center "Нет доступа к странице"]
          [:br] [:br]
          [:p  "Проверьте правильность "
            [:a {:href (-> conf :urls :login)} "входа на сайт"]
            "."]
          [:br]])})


(defn not-found [req]
  {:status 404
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body
      (layout req {:title "Страница не найдена"}
        [:div.jumbotron {:style "padding: 6em 8em"}
          [:h2 "Страница по этому адресу отсутствует."]
          [:br]
          [:p "Попробуйте воспользоваться <a href='/search'>поиском</a>."]])})


(defn error-page [req msg]
  {:status 403
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body
      (layout req {}
        [:div.jumbotron.error
          [:h1 msg]])})


(defn wrap-user-required [handler]
  (fn [req]
    (if (:user req)
      (handler req)
      (no-access req))))
