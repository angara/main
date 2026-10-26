(ns notify.process.worker
  (:require
   [mount.core :refer [defstate]]
   [taoensso.telemere :refer [log!]]
   ;
   [mlib.thread :refer [start-loop stop-loop]]
   [mlib.core :refer [now-ms to-long]]
   [mlib.mail :refer [send-mail]]
   [notify.const :refer [WORKER_RETRY_DELAY
                         EVENT_TYPE_FORUM_MESSAGE
                         EVENT_TYPE_PRIVATE_MESSAGE
                         USER_ONLINE_INTERVAL
                         ONLINE_USER_NOTIFY_DELAY_MS]]
   [sql.user :refer [user-by-id user-online?]]
   [sql.notify :refer [user-unread-topic get-mail mail-read?
                       new-job peek-job select-job finish-job]]
   ,))


(def WORKER_SLEEP_MS 5000)

;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn send-private-notify [user from-user mail]
  (let [email (:email user)
        subject "Angara.Net: Личное сообщение"
        text (str
              "Cообщение от пользователя: " (:username from-user) "\n\n"
              "- - -\n" (:body mail) "\n- - -\n\n"
              "https://angara.net/mail/\n\n")]
    (when (send-mail email subject text)
      (log! ["send-mail.ok:" email subject]))))


(defn send-topic-notify [user topic]
  (let [email (:email user)
        subject (str "Angara.Net/forum: " (:title topic))
        text (str
              "Новое сообщение в теме:\n\n" (:title topic) "\n\n"
              "https://angara.net/forum/t" (:tid topic) "?unread\n\n")]
    (when (send-mail email subject text)
      (log! ["send-mail.ok:" email subject]))))


(defn forum-message [{user-id :user_id topic-id :topic_id :as job}]
  (when-let [topic (user-unread-topic user-id topic-id)]
    (if (user-online? user-id USER_ONLINE_INTERVAL)
      (new-job
        (+ (now-ms) ONLINE_USER_NOTIFY_DELAY_MS)
        (update job :deferred (fnil inc 0)))
      ;;
      (let [user (user-by-id user-id nil)]
        ;; NOTE: check notification type
        (send-topic-notify user topic)))))


(defn private-message [{user-id :user_id from-id :from_id mail-id :mail_id :as job}]
  (if-let [mail (get-mail (to-long mail-id))]
    (when-not (mail-read? mail)
      (if (user-online? user-id USER_ONLINE_INTERVAL)
        (new-job
          (+ (now-ms) ONLINE_USER_NOTIFY_DELAY_MS)
          (update job :deferred (fnil inc 0)))
        ;;
        (let [user (user-by-id user-id nil)
              from (user-by-id from-id nil)]
          (send-private-notify user from mail))))
    ;;
    (log! :warn ["private-message: mail message missing" mail-id])))


;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn do-job [job]
  (try
    (condp = (:type job)
      EVENT_TYPE_FORUM_MESSAGE    (forum-message job)
      EVENT_TYPE_PRIVATE_MESSAGE  (private-message job)
      (do
        (log! :warn ["do-job: unexpected type" job])
        nil
        ))
    (catch Exception ex
      (log! :warn ["do-job failed:" job (.getMessage ex)])
      nil)
    (finally
      (finish-job (:id job)))))


;; ;; ;; ;; ;; ;; ;; ;; ;;

(defn worker-init [_]
  (log! ["worker-init"]))


(defn worker-step [_]
  (try
    (if-let [job (select-job)]
      (do-job job)
      (let [next-job (peek-job)
            sleep (- (now-ms) (or (:inst next-job) 0))]
        (Thread/sleep (min WORKER_SLEEP_MS (max 0 sleep)))))
    (catch Exception ex
      (log! :warn ["worker-step:" (.getMessage ex)])
      (Thread/sleep WORKER_RETRY_DELAY))))


(defn worker-cleanup [state ex]
  (log! ["worker-cleanup:" state ex]))


(defstate queue-worker
  :start
  (start-loop worker-init worker-step worker-cleanup)
  :stop
  (stop-loop queue-worker))
