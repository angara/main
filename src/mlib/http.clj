(ns mlib.http
  (:require
   [jsonista.core :as j]
   ,))


(defn text-resp [body]
  {:status  200
   :headers {"Content-Type" "text/plain;charset=utf-8"}
   :body    (str body)})


(defn html-resp [body]
  {:status  200
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body    (str body)})


(defn json-resp
  ([body]
    {:status  200
     :headers {"Content-Type" "application/json;charset=utf-8"}
     :body    (j/write-value-as-string body)})
  ([status body]
    {:status  status
     :headers {"Content-Type" "application/json;charset=utf-8"}
     :body    (j/write-value-as-string body)}))

; 400 Bad Request
; 402 Payment Required
; 401 Unauthorized
; 403 Forbidden
; 404 Not Found
; 405 Method Not Allowed
; 429 Too Many Requests
;
; 500 Internal Server Error
; 501 Not Implemented
; 502 Bad Gateway
; 503 Service Unavailable

(defn json-err [body]
  (json-resp 400 body))


(defn json-syserr [body]
  (json-resp 500 body))


(defn json-request?
  "check for application/json content-type"
  [req]
  (when-let [ctype (get-in req [:headers "content-type"])]
    (seq (re-find #"^application/(.+\+)?json" ctype))))


(defn ajax? [request]
  (= "XMLHttpRequest" (get-in request [:headers "x-requested-with"])))


(defn make-url [sheme host uri qs]
  (str sheme "://" host uri (and qs (str "?" qs))))

