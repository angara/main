(ns web.middleware
  (:import 
   [java.util.concurrent.atomic AtomicLong]
  ,))


(defn wrap-throttle [handler {time-ms :time limit :limit}]
  (let [cnt (AtomicLong. 0)
        rst (AtomicLong. (System/currentTimeMillis))]
    (fn [req]
      (let [now (System/currentTimeMillis)]
        (if (> now (+ (.get rst) time-ms))
          (do
            (.set cnt 0)
            (.set rst now)
            (handler req))
          (if (<= (.incrementAndGet cnt) limit)
            (handler req)
            {:status 429
             :headers {"Content-Type" "text/plain"}
             :body "Too many requests"})))
      ,)))
