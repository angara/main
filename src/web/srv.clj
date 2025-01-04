
(ns web.srv
  (:require
    [mlib.logger :refer [info]]
    [ring.adapter.jetty :refer [run-jetty]]
    [mount.core :refer [defstate] :as mount]
    [mlib.config :refer [conf]]
    [mlib.web.middleware :refer [middleware]]
    [web.app :as app]
   ,))


(defn start [handler]
  (let [hc (-> conf :main :http)]
    (info "build -" (:build conf))
    (info "start server -" hc)
    (run-jetty handler hc)))


(defstate server
  :start
    (->
      (app/app-handler)
      (middleware)
      start)
  :stop
    (.stop server))
