
(ns forum.db
  (:require
    [clojure.string :as s]
    [clj-time.core :as tc]
    [honeysql.helpers :as h]
    ;
    [mlib.conf :refer [conf]]
;    [mlib.log :refer [warn]]
    [mlib.core :refer [to-int]]
    [sql.core :refer [fetch exec insert-into]]))
;

(def FORUM_LASTREAD (keyword "forum_lastread"))
; uid            | integer                     | not null
; tid            | integer                     | not null
; msgid          | integer                     | not null
; watch          | integer                     | not null default 0
; post_count     | integer                     | not null default 0
; last_post_time | timestamp without time zone |

(def FORUM_TOPICS (keyword "forum_topics"))
; tid        | integer                     | not null
; tgroup     | integer                     | not null
; owner      | integer                     | not null
; title      | character varying(400)      | not null
; created    | timestamp without time zone | not null
; lastupdate | timestamp without time zone | not null
; lastposter | integer                     |
; closed     | boolean                     | not null default false
; msgnum     | integer                     | not null default 0
; lastmsgid  | integer                     | not null default 0
; ordi       | integer                     | not null default 0

(def FORUM_MESSAGES (keyword "forum_msgs"))
; msgid      | integer                     | not null
; topic      | integer                     | not null
; owner      | integer                     | not null
; censored   | boolean                     | not null default false
; updated    | timestamp without time zone | not null
; body       | text                        | not null
; moder      | integer                     | not null default 0
; attach     | character varying(80)       |
; ipaddr     | character varying(40)       | not null
; topic_head | boolean                     | not null default false

(def FORUM_GROUP_FAV (keyword "forum_group_fav"))
;  uid    | integer                     |           | not null | 
;  grp    | integer                     |           | not null | 
;  ts     | timestamp without time zone |           | not null | now()

(def USERS (keyword "users"))
; uid       | integer                     | not null
; login     | character varying(40)       | not null
; active    | boolean                     | not null default false
; name      | character varying(80)       | not null
; email     | character varying(120)      | not null
; icq       | character varying(12)       |
; homepage  | character varying(120)      |
; password  | character varying(80)       | not null
; lastlogin | timestamp without time zone |
; note      | character varying(4000)     |
; created   | timestamp without time zone | not null
; roles     | character varying(20)       | not null
; town      | character varying(80)       |
; vercode   | character varying(20)       |
; surname   | character varying(40)       | not null
; usefn     | boolean                     | not null default true
; pubemail  | character varying(120)      |
; photo     | character varying(200)      |
; vip       | boolean                     | not null default false
; showemail | boolean                     | not null default false
; atime     | timestamp without time zone | not null default now()
; ntime     | timestamp without time zone | not null default now()
; last_ip   | character varying(40)       |
; apikey    | character varying(40)       |
; new_id    | character varying(40)       |
; forum_ban | timestamp with time zone    |
; mail_ban  | timestamp with time zone    |


(defn get-topic [tid]
  (->
    (h/select :*)
    (h/from FORUM_TOPICS)
    (h/where [:= :tid (to-int tid 0)])
    (fetch)
    (first)))
;


(defn get-messages [tid offset limit]
  (->
    (h/select :*)
    (h/from FORUM_MESSAGES)
    (h/where [:= :topic (to-int tid 0)])
    (h/order-by :msgid)
    (h/offset offset)
    (h/limit limit)
    (fetch)))
;

(defn attach-params
  "returns: [type '34/56/f_123456']"
  [msg]
  ;; sample field data  :attach "jpg 2073044"
  (when-let [type (-> msg :attach str (s/split #" +") first not-empty)]
    (let [id (-> msg :msgid str)
          padded (str "0000" id)
          len (.length padded)
          len-2 (- len 2)
          len-4 (- len 4)
          base (str (.substring padded len-4 len-2) "/"
                    (.substring padded len-2 len) "/" "f_" id)]
      {:type type :base base})))
;

(defn get-lastread [uid tid]
  (->
    (h/select :*)
    (h/from FORUM_LASTREAD)
    (h/where 
      [:= :uid (to-int uid 0)]
      [:= :tid (to-int tid 0)])
    (fetch)
    (first)))
;

(defn lastreads [uid tids]
  (->
    (h/select :*)
    (h/from FORUM_LASTREAD)
    (h/where
      [:= :uid (to-int uid 0)]
      [:in :tid tids])))

(defn update-watch [uid tid watch]
  (->
    (h/update :FORUM_LASTREAD)
    (h/sset {:watch watch})
    (h/where 
      [:= :uid uid]
      [:= :tid tid])
    (exec)
    (= 1)))
;

(defn insert-watch [uid tid watch]
  (or
    (update-watch uid tid watch)
    (->
      (h/insert-into :FORUM_LASTREAD)
      (h/values [{:uid    uid 
                  :tid    tid 
                  :msgid  0 
                  :watch  watch
                  :last_post_time (tc/now)}])
      (exec)
      (= 1))))
;

;;.
