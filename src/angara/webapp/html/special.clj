(ns angara.webapp.html.special
  (:require
    [angara.webapp.html.core :refer [html]]
    [angara.webapp.html.layout :as layout]
   ))


(defn not-found [req title]
  (layout/frame
      (assoc req :page {:title title})
      (html

  [:div.p-6.shadow-lg.rounded-lg.bg-gray-100.text-gray-700
    ; [:h2.font-semibold.text-3xl.b-5 "Тема форума не найдена"]
   ;; TODO
       [:p "[в разработке]"]
       [:p "Переход на список тем форума, переход в начало сайта, поиск по сайту ..."]
       [:p " - - - "]
    ;<hr class="my-6 border-gray-300" />
  ;; <p>
  ;;   It uses utility classes for typography and spacing to space content out within the larger
  ;;   container.
  ;; </p>
  ;; <button
  ;;   type="button"
  ;;   class="inline-block px-6 py-2.5 mt-4 bg-blue-600 text-white font-medium text-xs leading-tight uppercase rounded shadow-md hover:bg-blue-700 hover:shadow-lg focus:bg-blue-700 focus:shadow-lg focus:outline-none focus:ring-0 active:bg-blue-800 active:shadow-lg transition duration-150 ease-in-out"
  ;;   data-mdb-ripple="true"
  ;;   data-mdb-ripple-color="light"
  ;; >
  ;;   Learn more
       ]
       )))

(defn error-page [req msg _data]
  (layout/frame
    (assoc req :page {:title "Ошибка сервера"})
    (html
      [:div.m-6.p-6.shadow-lg.rounded-lg.bg-gray-100.text-gray-700
      [:p [:b "Сообщение:"]]
      [:br]
      [:p msg]])))

