(ns notify.const
  (:require
    [tick.core :as t]))
  
;; global constants

(def WORKER_RETRY_DELAY    5000)

(def EVENT_TYPE_FORUM_TOPIC       "forum_topic")
(def EVENT_TYPE_FORUM_MESSAGE     "forum_message")
(def EVENT_TYPE_FORUM_MODER       "forum_moder")
(def EVENT_TYPE_PRIVATE_MESSAGE   "private_message")
;; deprecated
(def EVENT_TYPE_FORUM_MSG         "forum_msg")


(def EVENT_TYPE_USER_REGISTER     "user_register")
(def EVENT_TYPE_USER_LOGIN        "user_login")


(def USER_ONLINE_INTERVAL (t/of-seconds 200))
(def ONLINE_USER_NOTIFY_DELAY_MS (* 240 1000))

;;.
