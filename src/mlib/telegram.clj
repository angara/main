
(ns mlib.telegram
  (:require
    [taoensso.timbre :refer [debug info warn]]
    [clj-http.client :as http]
    [mlib.conf :refer [conf]]))
;


(defn api-url [token method]
  (str "https://api.telegram.org/bot" token "/" (name method)))

(defn token []
  (-> conf :telegram :token))

(defn timeout []
  (-> conf :telegram (:timeout 5000)))


(defn api
  ( [method params]
    (api (token) method params))
  ;
  ( [token method params]
    (try
      (let [tout (timeout)
            res (:body
                  (http/post (api-url token method)
                    { :content-type :json
                      :as :json
                      :form-params params
                      :socket-timeout tout
                      :conn-timeout tout}))]
        (if (:ok res)
          (:result res)
          (info "api-fail:" method res)))
      (catch Exception e (warn "api:" method e)))))
;

(defn send-text [chat text]
  (api :sendMessage {:chat_id chat :text text}))
;

(defn send-markdown [chat text]
  (api :sendMessage {:chat_id chat :text text :parse_mode "Markdown"}))
;

(defn send-message [chat params]
  (api :sendMessage (merge {:chat_id chat} params)))
;

(defn send-file
  "params should be stringable (json/generate-string)
    or File/InputStream/byte-array"
  ( [method multipart-params]
    (send-file (token) method multipart-params))
  ;
  ( [token method mpart]
    (try
      (let [tout (timeout)
            res (:body
                  (http/post (api-url token method)
                    { :multipart
                        (for [[k v] mpart]
                          {:name (name k) :content v :encoding "utf-8"})
                      :as :json
                      :socket-timeout tout
                      :conn-timeout tout}))]
            ;
        (if (:ok res)
          (:result res)
          (info "send-file:" method res)))
      (catch Exception e (warn "send-file:" method e)))))
;


(defn set-webhook-cert [token url cert-file]
  (http/post (api-url token :setWebhook)
    {:multipart [ {:name "url" :content url}
                  {:name "certificate" :content cert-file}]}))
;

;;.
