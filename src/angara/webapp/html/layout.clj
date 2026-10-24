(ns angara.webapp.html.layout
  (:require 
    [angara.webapp.html.core :refer [html doctype-html]]
    [angara.webapp.html.util :refer [make-url]]
   ))


(defn render-page [head body]
   (html doctype-html 
         [:html 
           [:head head] 
           [:body.min-h-screen.flex.flex-col 
            body]]))


(def DESCR_DEFAULT
  (str "Активный отдых, спорт, туризм, треки, путешествия, фото."
       " Новости экстремального спорта, календарь событий, фотографии."
       " Байкал и Прибайкалье. Прогноз погоды."))


(defn head-meta [{:keys [asset-fn page uri query-string]}]
  (let [{:keys [title window-title descr]} page
        wtl (or window-title title)
        descr (or descr DESCR_DEFAULT)
        url (make-url "https" "angara.net" uri query-string)
        og-image (asset-fn "/img/angara-og.png")]
    (html
     [:meta {:charset "utf-8"}]
     [:title "Angara.Net" (and wtl (str ": " wtl))]
     "\n"
     [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
     [:meta {:name "description" :content descr}]
     [:meta {:property "og:site_name" :content "Angara.Net"}]
     [:meta {:property "og:type"   :content "article"}]
     [:meta {:property "og:locale" :content "ru_RU"}]
     [:meta {:property "og:image"  :content og-image}]
     [:meta {:property "og:title"  :content title}]
     [:meta {:property "og:url"    :content url}]
     [:meta {:property "og:description" :content descr}]
     [:link {:rel "shortcut icon" :href (asset-fn "/img/favicon.ico")}])
    ,))


(defn head-styles [{:keys [asset-fn]}]
  (html
     ;; https://google-webfonts-helper.herokuapp.com/fonts/roboto?subsets=cyrillic,latin
     [:link {:rel "stylesheet" :type "text/css" :href (asset-fn "/fonts/roboto.css")}]
     ; [:link {:rel "stylesheet" :type "text/css" :href (asset-fn "/tw-elements.min.css")}]
     [:link {:rel "stylesheet" :type "text/css" :href (asset-fn "/main.css")}]
     ; [:script {:type "text/javascript" :defer 1 :src  (asset-fn "/tw-elements.min.js")}]
     [:script {:type "text/javascript" :defer 1 :src  (asset-fn "/htmx.min.js")}]
   ))


(defn user-info-block [user]
  [:div.flex.flex-col.justify-center.leading-5
   [:div.text-right
     [:a.text-sky-700 {:href "/me"} (:first_name user) " " (:last_name user)]]
   (when-let [username (:username user)]
     [:div.text-right
       [:a.font-semibold.text-sky-500 {:href "/me"} username]]     
     )
   ])


(defn frame [{:keys [asset-fn page user] :as req} & content]
  (let [page-title (:title page)]
    (render-page
     (list
      "\n"
      (head-meta req)
      "\n"
      (head-styles req))
     (html
      [:header.mb-2.border-b.border-blue-400
       [:div {:class "flex flex-wrap flex-row md:container md:mx-auto p-1 md:px-2 md:py-1"}
        [:div.justify-start.p-2
         [:a {:href "https://angara.net/"}
          [:img.inline-block {:src (asset-fn "/img/angara_310.png") :alt "Angara.Net" :width "310" :height "58"}]]]

        [:div.p-2.grow.flex.justify-center.flex-col
         ; [:div.h-max.text-red-700.self-center "В разработке!"]
         ]

        [:div.p-2.flex.w-60.justify-end.overflow-clip
         (when user
           (user-info-block user))
         ]]]
    ;;
      [:article {:class "md:flex md:flex-row grow py-1"}
       [:div {:class "container mx-auto px-1"}
        (when page-title
          [:h1.text-indigo-700.text-2xl.font-bold.text-center.w-full.pb-2.px-2.m-1.tracking-wide page-title])
        content
        ]
       ;; menu
       ]
    ;;
      [:footer.mt-2.border-t.border-blue-400.bg-slate-100.py-2
       [:div {:class "flex flex-row flex-wrap md:container md:mx-auto px-2 py-1 justify-items"}
        [:div.mx-1 [:br]]
        [:div.grow [:br]]
        [:div.mx-1.flex.items-center.text-end.text-sm
         [:a.font-semibold.text-sky-500 {:href "https://angara.net/"} "Angara.Net"] 
         [:span.text-gray-400.ml-1.italic " © 2002-2026"]]
        ]
       ]
      ;; metrics
      ))))
