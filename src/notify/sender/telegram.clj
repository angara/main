(ns notify.sender.telegram
  (:require
   [taoensso.telemere :refer [log!]]
   [clojure.pprint :refer [pprint]]
   ;
   [mlib.tgapi :refer [esc send-message]]
   [app.config :refer [conf]]
   [notify.const :refer [EVENT_TYPE_FORUM_MESSAGE]]
   ,))


(def NOTIFY_TEXT_MAX 2000)
(def NOTIFY_MSG_MAX 3000)


(defn truncate-text [s]
  (if (>= (.length s) NOTIFY_TEXT_MAX)
    (str (subs s 0 NOTIFY_TEXT_MAX) " ...")
    s))


(defn format-event [event]
  (let [type  (:type event)
        body  ((keyword type) event)
        uid   (:user_id body)
        eid   (str "<pre>eid:" (:eid event) "</pre>")
        hdr   (str "<b>" type "</b>" " 🔹 user:" uid "\n")
        topic (when (= type EVENT_TYPE_FORUM_MESSAGE)
                (str "https://angara.net/forum/t" (:topic_id body) "#" (:msg_id body)))
        title (esc (str (:title body)))
        text  (esc (truncate-text (str (:text body))))
        attr  (dissoc body :text :user_id :title :topic_id :msg_id)
        attr  (when-not (empty? attr)
                (esc (with-out-str (pprint attr))))
        ,]
    (if (< NOTIFY_MSG_MAX (+ (.length title) (.length text)))
      (str hdr "\n<b>Very long message truncated!</b>" eid)
      ;;
      (str hdr
        (when topic
          (str topic "\n"))
        (when attr
          (str "<pre>" attr "</pre>\n"))
        "\n"
        (when (< 0 (.length title))
          (str "🔸 <b>" title "</b>\n"))
        text "\n\n" eid))))


(defn firehose [event]
  (try
    (let [cfg  (-> conf :notify :telegram)
          chan (-> conf :notify :firehose :channel)
          html (format-event event)]
      (send-message cfg chan html))
    (catch Exception ex
      (log! ["firehose:" ex (ex-data ex)]))))
