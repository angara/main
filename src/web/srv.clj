
(ns web.srv
  (:require
    [taoensso.timbre :refer [info warn]]
    [ring.adapter.jetty :refer [run-jetty]]
    [mount.core :refer [defstate] :as mount]
    [mlib.conf :refer [conf]]
    [mlib.web.middleware :refer [middleware]]
    [web.app :as app]))
;

(defn start [handler]
  (let [hc (:http conf)]
    (info "build -" (:build conf))
    (info "start server -" hc)
    (run-jetty handler hc)))
;

(defstate server
  :start
    (->
      (app/app-handler)
      (middleware)
      start)
  :stop
    (.stop server))
;

;;.
