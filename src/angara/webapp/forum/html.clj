(ns angara.webapp.forum.html
  (:require
    [angara.lib.time :refer [ddmmyyyy hhmm]]
    [angara.webapp.html.core :refer [html raw]]
    [angara.webapp.forum.pager :refer [pager-block]]
    [angara.webapp.forum.topic :refer [is-updated? format-msg-text format-attach]]
   ))

;; https://www.vertex42.com/ExcelTips/unicode-symbols.html


(defn message [msg unread?] 
  (let [msg-id (:msgid msg)
        author-name (str (:first_name msg) " " (:last_name msg))
        username (:username msg)
        updated-at (:updated msg)
        updated? (is-updated? (:created_at msg) updated-at)
        user-link (str "/user/" username)
        msg-time (str (ddmmyyyy updated-at) (if updated? " + " " - ") (hhmm updated-at))
        ]
    ;
    (html
     [:div.mx-2.my-4.rounded-md.shadow
      "\n"
      [:a {:name (str msg-id)}]
      [:div.border-b.flex.text-sm {:class (str "py-0.5 px-1.5 " (if unread? "bg-sky-100" "bg-stone-100"))}
       [:div.grow
        [:span.text-stone-500 (if unread? "◆" "◇")]  ; » ◇ ◆ ⬫
        " "
        [:span.text-indigo-800 {:class "mx-0.5"} author-name]
        [:span.text-stone-400 " / "]
        [:a.alink {:href user-link} "@" username]]
       [:div.text-gray-600.flex
         ;; right
        ]]

      [:div.leading-tight.text-gray-700.break-words {:class "px-3 py-0.5 my-1"} 
        (raw (format-msg-text msg))
        (format-attach msg)
       ]
     ;
      [:div.border-t.border-dotted.flex.text-xs
       [:div.grow.flex.items-center.text-stone-400 {:class "mx-2.5 my-0.5 cursor-pointer"} "#" 
          [:span.text-emerald-600 msg-id]]
      ; [:div]
      ; [:span.text-xs.mx-2 "🖌"
       [:div.text-indigo-500.text-right.flex.items-center {:class "mx-2.5 my-0.5"} msg-time]]]
       )))


(defn messages-block [topic-base-uri msgs curr-page num-pages lastread]
  (let [pager (pager-block
                    #(str topic-base-uri "?page=" %)
                    curr-page num-pages)]
    (html
     [:div.mx-0 {:class "lg:mx-4"}
      (when pager
        [:div.my-4.text-sm pager]) 

      (for [msg msgs 
            :let [is-unread (> (:msgid msg) lastread)]]
        (message msg is-unread))

      (when pager
        [:div.my-4.text-sm pager])])
    ))
