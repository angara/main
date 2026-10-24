(ns angara.webapp.forum.handlers
  (:require
   [tick.core :as t]
   ;
   [mlib.core :refer [to-long]]
   [angara.lib.time :refer [ddmmyyyy hhmm]]
   [angara.webapp.http.resp :refer [throw-resp!]]
   [angara.webapp.html.core :refer [html-resp html-status-resp]]
   [angara.webapp.html.layout :as layout]
   [angara.webapp.html.special :refer [not-found]]
   [angara.webapp.forum.html :refer [messages-block]]
   [angara.webapp.forum.group :refer [GROUP_PAGE_SIZE]]
   [angara.webapp.forum.topic :refer [TOPIC_PAGE_SIZE]]
   [angara.webapp.forum.pager :refer [pager-block bound-int num-pages]]
   [angara.webapp.forum.db :as fdb]
   ))


;; :user comes from the main app chain (web.app/wrap-user), uid is a string

(defn- forum-user-id [req]
  (some-> req :user :id to-long))


(defn group-link [group-id]
  (str "/forum/group/" group-id))


(defn topic-link [topic-id]
  (str "/forum/topic/" topic-id))


(defn username-link [username]
  (str "/user/" username))


(def UNREAD_MARK_DAYS (t/new-duration 15 :days))


(defn h-group-list [req]
  (let [user-id (forum-user-id req)
        groups (fdb/forum-groups)
        unread-groups (when user-id
                        (set (fdb/user-unread-groups user-id (t/<< (t/date-time) UNREAD_MARK_DAYS))))
       ]
    (-> (assoc req :page {:title "Разделы форума"})
     (layout/frame
      [:div.m-3.mb-6
        ; :lastpost_username :ord :lastupdate :lastpost_last_name :lastpost_uid :title :topic_id :group_id :lastpost_first_name
        (for [{:keys [group_id title lastpost_username lastupdate]} groups
              :let [is-unread (get unread-groups group_id)]]
          [:div.my-1.py-1.bg-stone-100.rounded.flex.items-center
            {:class
              (if is-unread
                "lg:mx-4 hover:bg-sky-200 border-x-2 border-sky-400"
                "lg:mx-4 hover:bg-sky-200"
                )
             }
           [:a.grow.text-blue-700.text-lg.mx-4.leading-tight {:href (group-link group_id)} title]
           [:div.flex.flex-col.text-sm.mx-2
            [:span.text-gray-700 (ddmmyyyy lastupdate) " - " (hhmm lastupdate)]
            [:a.text-blue-700.text-center {:href (username-link lastpost_username)} lastpost_username]]
           ]
          )
       ])
       (html-resp)
     )))


(defn h-group [{:keys [params path-params] :as req}]
  (let [user-id (forum-user-id req)
        group (some-> path-params :group_id to-long fdb/forum-group-by-id)
        _ (when-not group
            (throw-resp! (html-status-resp 404 (not-found req "Раздел форума не найден."))))
        group-id (:group_id group)
        group-title (:title group)
        topic-count (fdb/group-topics-count group-id)
        num-pages (num-pages topic-count GROUP_PAGE_SIZE)
        curr-page (-> params :page (to-long) (bound-int 1 num-pages))
        topics (fdb/group-topics group-id (* (dec curr-page) GROUP_PAGE_SIZE) GROUP_PAGE_SIZE)
        unreads (when user-id
                  (set (fdb/group-topics-unreads user-id (map :topic_id topics))))

        pager  (pager-block
                  #(str (group-link group-id) "?page=" %)
                  curr-page num-pages)]

    (-> req
        (assoc :page {:title (str "Форум: " group-title)})
        (layout/frame
         [:div.m-3.mb-6
            (when pager [:div.text-sm.m-3 pager])
            (for [{:keys [topic_id title msgnum closed lastupdate lastpost_username]} topics
                   :let [is-unread (get unreads topic_id)]]
              [:div.my-1.py-1.bg-stone-100.rounded.flex.items-center
               {:class (if is-unread
                         "lg:mx-4 hover:bg-sky-200 border-x-2 border-sky-400"
                         "lg:mx-4 hover:bg-sky-200")}
               [:a.grow.text-blue-700.text-lg.mx-4.leading-tight {:href (topic-link topic_id)}
                  title (when closed [:span.text-gray-800.text-sm.mx-1 " 🔒 "]) ;; U+1F512
                ]
               [:div.mx-5.text-sm.flex.items-center.text-gray-500 msgnum]
               [:div.flex.flex-col.text-sm.mx-2
                [:span.text-gray-700.mx-1  (ddmmyyyy lastupdate) " - " (hhmm lastupdate)]
                [:a.text-blue-700.text-center {:href (username-link lastpost_username)} lastpost_username]]]
              )
            (when pager [:div.text-sm.m-3 pager])
          ]
         )
        (html-resp)
        )))


(defn h-topic [{:keys [params path-params] :as req}]
  (let [user-id (forum-user-id req)
        topic-info (some-> path-params :topic_id to-long fdb/topic-info)
        _ (when-not topic-info
            (throw-resp! (html-status-resp 404 (not-found req "Тема форума не найдена"))))
        ;
        topic-id (:tid topic-info)
        title (:title topic-info)
        num-pages (num-pages (:msgnum topic-info) TOPIC_PAGE_SIZE)
        ;
        curr-page (-> params :page (to-long) (bound-int 1 num-pages))
        msgs (fdb/topic-messsages topic-id (* (dec curr-page) TOPIC_PAGE_SIZE) TOPIC_PAGE_SIZE)
        lastread (or
                    (when user-id
                      (fdb/topic-lastread topic-id user-id))
                    0)

        topic-base-uri (str "/forum/topic/" topic-id)
        ]
    (html-resp
     (layout/frame
        (assoc req :page {:title title :window-title title})
        (messages-block topic-base-uri msgs curr-page num-pages lastread)
      ))))
