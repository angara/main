(ns notify.process.inbound
  (:require
   [mount.core :refer [defstate]]
   [taoensso.telemere :refer [log!]]
   ;
   [mlib.thread :refer [start-loop stop-loop]]
   [mlib.core :refer [now-ms]]
    [notify.const :as C]
    [sql.notify-event :as events]
   [sql.notify :refer [topic-unread-watch new-job]]
   [notify.process.user :as user]
   [notify.sender.telegram :refer [firehose]]
   ,))


;; ;; ;; ;; ;; private message ;; ;; ;; ;; ;;

(defn hide-text [s]
  (str "[Private text: " (count s) " chars]"))


(defn private-message [event]
  (firehose (update-in event [:private_message :text] hide-text))
  (let [msg (:private_message event)]
    (new-job
      (now-ms)
      { :type C/EVENT_TYPE_PRIVATE_MESSAGE
        :mail_id (:id msg)
        :from_id (:user_id msg)
        :user_id (:to_id msg)})))


;; ;; ;; ;; ;; forum message ;; ;; ;; ;; ;;

(defn forum-message [event]
  (firehose event)

  (let [topic-id (get-in event [:forum_message :topic_id])
        subs (topic-unread-watch topic-id)]
    (doseq [s subs]
      (new-job
        (now-ms)
        { :type C/EVENT_TYPE_FORUM_MESSAGE
          :topic_id topic-id
          :user_id (str (:uid s))}))))


(defn forum-topic [event]
  (log! ["forum-topic:" (dissoc (:forum_topic event) :text)])
  (firehose event))


(defn forum-moder [event]
  (log! ["forum-moder:" event])
  (firehose event))


;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn- fixup-forum-msg [event]
  (-> event
    (assoc
      :type C/EVENT_TYPE_FORUM_MESSAGE
      :forum_message (:forum_msg event))
    (dissoc :forum_msg)))


(defn dispatch [event]
  (when event
    (condp = (:type event)
      C/EVENT_TYPE_FORUM_MSG       (-> event fixup-forum-msg forum-message)
      ;;
      C/EVENT_TYPE_FORUM_MESSAGE   (forum-message event)
      C/EVENT_TYPE_FORUM_TOPIC     (forum-topic   event)
      C/EVENT_TYPE_FORUM_MODER     (forum-moder   event)
      C/EVENT_TYPE_PRIVATE_MESSAGE (private-message event)
      C/EVENT_TYPE_USER_REGISTER   (user/register event)
      C/EVENT_TYPE_USER_LOGIN      (user/login    event)
      (do
        (log! :warn ["unexpected event:" event])
        (firehose event)))))


(defn feeder-init [_]
  (log! ["notify_event feeder started"]))

(defn feeder-step [_]
  (try
    (if-let [row (events/claim)]
      (try
        (events/process! row dispatch)
        (catch Exception ex
          (events/retry! row ex)
          (log! :warn ["notify_event failed" (:id row) (.getMessage ex)])))
      (Thread/sleep 2000))
    (catch Exception ex
      (log! :warn ["feeder-step:" (.getMessage ex)])
      (Thread/sleep 5000))))


(defn feeder-cleanup [state ex]
  (log! ["feeder-cleanup:" state ex]))


(defstate feeder
  :start
  (start-loop feeder-init feeder-step feeder-cleanup)
  :stop
  (stop-loop feeder))
