(ns mlib.mail
  (:require
    [taoensso.telemere :refer [log!]]
    [postal.core :refer [send-message]]
    [app.config :refer [conf]]
    ,))


(defn send-mail [to subj text]
  (try
    (let [smtp  (-> conf :sendmail :smtp)
          envel (-> conf :sendmail :envel)]
      (-> (send-message smtp
            (merge envel
              { :type "text/plain;charset=utf-8"
                :to [to]
                :subject subj
                :body text}))
          :error (= :SUCCESS)))
    (catch Exception ex
      (log! :warn ["send-mail:" ex]))))
