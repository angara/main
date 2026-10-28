(ns angara.webapp.app.middleware
  (:require
    [clojure.string :as str]
    [taoensso.telemere :refer [log!]]
    ;
    [angara.webapp.http.resp :refer [throw-resp! EXCEPTION_RESPONSE_KEY]]
    [angara.webapp.html.core :refer [html-status-resp]]
    [angara.webapp.html.special :refer [error-page]]
  ))


(def WEBAPP_ASSETS "/webapp/assets")


(defn wrap-request-config [handler]
  (fn [req]
    (-> req
        (assoc
          :webapp-assets WEBAPP_ASSETS
          :asset-fn (fn [path] (str WEBAPP_ASSETS path)))
        (handler))))


(defn wrap-error-page [handler]
  (fn [req]
    (try
      (handler req)
      (catch java.sql.SQLException ex
        (throw ex))
      (catch Exception ex
        (let [data (ex-data ex)]
          (if (get data EXCEPTION_RESPONSE_KEY)
            (throw ex)
            (let [msg (ex-message ex)]
              (log! :warn ["page exception:" msg data])
              (throw-resp! (html-status-resp 500 (error-page req msg data))))))))))


;; top-level catch: throw-resp! ex-info -> ring response
;; (from angara.webapp.http.server, aleph stack is gone)
(defn wrap-resp-exceptions [handler]
  (fn [req]
    (try
      (handler req)
      (catch java.sql.SQLException ex
        (throw ex))
      (catch Throwable ex
        (let [data (ex-data ex)
              message (ex-message ex)]
          (if-let [resp (get data EXCEPTION_RESPONSE_KEY)]
            (do
              (when-not (str/blank? message)
                (log! :info [message data]))
              resp)
            (let [msg (str "exception: " message)]
              (log! :warn [msg data ex])
              {:status 500 :headers {"Content-Type" "text/plain"} :body msg})))))))
