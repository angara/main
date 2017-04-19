
(ns web.middleware
  (:import
    [java.util.concurrent.atomic AtomicLong]))


(defn- ^Long now-ms []
  (System/currentTimeMillis))
;

(defn wrap-throttle
  "time intervale in milliseconds"
  [handler {time :time limit :limit}]
  ;
  (let [cnt (AtomicLong. 0)
        rst (AtomicLong. (now-ms))]
    ;
    (fn [req]
      (let [now (now-ms)]
        (if (> now (+ (.get rst) time))
          (do
            (.set cnt 0)
            (.set rst now)
            (handler req))
          (if (<= (.incrementAndGet cnt) limit)
            (handler req)
            {:status 429
             :headers {"Content-Type" "text/plain;"}
             :body "Too many requests"}))))))
;

;;.
