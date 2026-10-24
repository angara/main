(ns angara.webapp.forum.db
  (:require
    [tick.core :as t]
    [honey.sql :as sql]
    [honey.sql.helpers :as h]   
    [angara.webapp.db.psql :refer [exec! exec-one!]]
   ))


(def GROUP_TOPIC_JOIN
  (->
   (h/select :g.*
             :t.lastupdate
             [:t.tid :topic_id]
             [:u.uid :lastpost_uid]
             [:u.login :lastpost_username]
             [:u.name :lastpost_first_name]
             [:u.surname :lastpost_last_name]
             )
   (h/from [:forum_group :g])
   (h/left-join [:forum_topics :t] [:= :t.tgroup :g.group_id])
   (h/left-join [:users :u] [:= :t.lastPoster :u.uid])
  ))


(def DISTINCT_GROUP_TOPIC 
  (->
      (h/select-distinct-on [:gt.group_id] :gt.*)
      (h/from [GROUP_TOPIC_JOIN :gt])
      (h/order-by [:gt.group_id] [:gt.lastupdate :desc])
  ))


(defn forum-groups [] 
  (->
     (h/select :dgt.*)
     (h/from [DISTINCT_GROUP_TOPIC :dgt])
     (h/order-by :dgt.ord)
     (sql/format)
     (exec!)
   ))


(defn forum-group-by-id [group-id]
  (->
   (h/select :*)
   (h/from :forum_group)
   (h/where [:= :group_id group-id])
   (sql/format)
   (exec!)
   (first)))


(defn group-topics-count [group-id]
  (->
    (h/select :%count.*)
    (h/from [:forum_topics :ft])
    (h/where [:= :ft.tgroup group-id]) 
    (sql/format)
    (exec-one!)
    (:count)
   ))


(def TOPIC_UPDATER_JOIN
  (->
    (h/select [:t.tgroup    :group_id]
              [:t.tid       :topic_id]
              [:t.ordi      :ordi]
              [:t.title     :title]
              [:t.closed    :closed]
              [:t.msgnum    :msgnum]
              [:t.lastupdate  :lastupdate]
              [:u.uid         :lastpost_uid]
              [:u.login       :lastpost_username]
              [:u.name        :lastpost_first_name]
              [:u.surname     :lastpost_last_name])
    (h/from [:forum_topics :t])
    (h/left-join [:users :u] [:= :u.uid :t.lastposter])
    (h/where [:= :t.tgroup :?group-id])
   ))


(defn group-topics [group-id offset limit]
  (->
    (h/select :*)
    (h/from [TOPIC_UPDATER_JOIN :t])
    (h/order-by [:ordi :desc] [:lastupdate :desc])
    (h/offset :?offset)
    (h/limit :?limit)
    (sql/format {:params {:group-id group-id :offset offset :limit limit}})
    (exec!)
   ))


(defn group-topics-unreads [user-id ids]
  (->
    (h/select [:t.tid :topic_id])
    (h/from [:forum_topics :t])
    (h/left-join [:forum_lastread :lr] [:and [:= :t.tid :lr.tid] [:= :lr.uid :?user-id]])
    (h/where [:in :t.tid :?ids]
             [:> :t.lastmsgid [[:coalesce :lr.msgid [:inline 0]]]]
             )
    (sql/format {:params {:user-id user-id :ids ids}})
    (exec!)
    (as-> x (map :topic_id x))
   ))


(def GROUP_TOPIC_LR_JOIN
  (->
    (h/select [:t.tgroup :group_id]
              [:t.tid :topic_id] 
              :t.lastmsgid 
              :t.lastupdate
              [[:coalesce :lr.uid :?user-id] :user_id]
              [[:coalesce :lr.msgid [:inline 0]] :msgid]
              )
    (h/from [:forum_topics :t])
    (h/left-join [:forum_lastread :lr] [:= :t.tid :lr.tid])
    (h/where [:> :t.lastupdate :?after]
    )))


(defn user-unread-groups [user-id after]
  (->
    (h/select :tlr.*)
    (h/from [GROUP_TOPIC_LR_JOIN :tlr])
    (h/order-by [:lastupdate :desc])
    (h/where [:= :tlr.user_id :?user-id]
             [:> :tlr.lastmsgid :tlr.msgid]
             )
    (sql/format {:params {:user-id user-id :after after}})
    (exec!)
    (as-> x
          (map :group_id x))
   ))


(comment

  (->>
   (user-unread-groups 1 (t/date "2022-08-20"))
   ;(remove #(= (:lastmsgid %) (:msgid %)))
   ;(remove #(:uid %))
   ;(remove #(:msgid %))
   #_(count)
   (time)
   )

  )


(defn topic-info [topic-id]
  (-> 
   (h/select :*)
   (h/from :forum_topics)
   (h/where [:= :tid topic-id])
   (sql/format)
   (exec-one!)
   ))


(comment
  
  (topic-info 37013)
  ;; => {:closed true,
  ;;     :created #object [java.time.LocalDateTime 0x6044c1ce "2011-04-25T00:17:49.757216"],
  ;;     :lastmsgid 541698,
  ;;     :lastposter 572,
  ;;     :lastupdate #object [java.time.LocalDateTime 0x1745bbef "2011-05-15T12:45:57.118347"],
  ;;     :msgnum 598,
  ;;     :ordi 0,
  ;;     :owner 572,
  ;;     :tgroup 11,
  ;;     :tid 37013,
  ;;     :title "ВелоАршан - 7-9 мая"}

  ,)


(defn q-topic-with-users [topic-id]
  (->
   (h/select :t.* 
             [:own_u.uid :owner_uid]
             [:own_u.login :owner_username]
             [:own_u.name :owner_first_name]
             [:own_u.surname :owner_last_name]
             [:lst_u.uid :lpost_uid] 
             [:lst_u.login :lpost_username]
             [:lst_u.name :lpost_first_name]
             [:lst_u.surname :lpost_last_name]
             )
   (h/from [:forum_topics :t])
   (h/left-join [:users :own_u] [:= :t.owner :own_u.uid])
   (h/left-join [:users :lps_u] [:= :t.lastPoster :lps_u.uid])
   (h/where [:= :t.tid topic-id])
   ))


(defn topic-messsages [topic-id offset limit]
  (-> 
   (h/select :m.* [:u.uid :user_id] [:u.login :username] [:u.name :first_name] [:u.surname :last_name])
   (h/from [:forum_msgs :m])
   (h/where [:= :topic topic-id])
   (h/order-by [:msgid :asc])
   (h/left-join [:users :u] [:= :m.owner :u.uid])
   (h/offset offset)
   (h/limit limit)
   (sql/format)
   (exec!)
   ))


(defn topic-lastread [topic-id user-id]
  (->
   (h/select :*)
   (h/from :forum_lastread)
   (h/where [:= :tid topic-id] [:= :uid user-id])
   (sql/format)
   (exec-one!)
   (:msgid)
   ))


(comment
  
  (topic-info 24589)

  ;(topic-message-count 24589)

  (topic-messsages 24589 0 2)
  ;; => [{:attach nil,
  ;;      :body
  ;;        "Не открываются wpt файлы для загрузки в gps, выложенные на сайте (раздел маршруты по Прибайкалью). Пробовал OziExplorer'ом, GPSDump разными версиями. Ошибка 5-й строки. Что не правильно делаю?",
  ;;      :censored false,
  ;;      :ipaddr "91.185.59.149",
  ;;      :moder 0,
  ;;      :msgid 381327,
  ;;      :owner 1609,
  ;;      :topic 24589,
  ;;      :topic_head false,
  ;;      :updated #object [java.time.LocalDateTime 0x10289036 "2010-07-10T14:17:29.151947"]}
  ;;     {:attach nil,
  ;;      :body "strigun [381327]: Чем сливались эти файлы с сайта? И какой именно глючит?",
  ;;      :censored false,
  ;;      :ipaddr "92.126.11.243",
  ;;      :moder 0,
  ;;      :msgid 381371,
  ;;      :owner 1,
  ;;      :topic 24589,
  ;;      :topic_head false,
  ;;      :updated #object [java.time.LocalDateTime 0x1c7eebfb "2010-07-10T19:36:05.840225"]}]

  
  (sql/format (q-topic-with-users 123))
  ;; => ["SELECT t.*, own_u.uid AS owner_uid, 
  ;;                  own_u.login AS owner_username, own_u.name AS owner_first_name, own_u.surname AS owner_last_name, 
  ;;                  lst_u.uid AS lpost_uid, lst_u.login AS lpost_username, lst_u.name AS lpost_first_name, 
  ;;                  lst_u.surname AS lpost_last_name 
  ;;     FROM forum_topics AS t LEFT JOIN users AS own_u ON t.owner = own_u.uid 
  ;;     LEFT JOIN users AS lps_u ON t.lastPoster = lps_u.uid 
  ;;     WHERE t.tid = ?"
  ;;     123]

  ,)
