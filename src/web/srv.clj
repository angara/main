(ns web.srv
  (:require
    [taoensso.telemere :refer [log!]]
    [ring.adapter.jetty :refer [run-jetty]]
    [mount.core :refer [defstate]]
    [app.config :refer [conf]]
    [mlib.web.middleware :refer [middleware]]
    [web.app :refer [app-handler]]
   ,))


(defn start [handler]
  (let [hc (-> conf :main :http)]
    (log! "start server -" hc)
    (run-jetty handler hc)))


(defstate server
  :start
    (->
      (app-handler)
      (middleware)
      (start))
  :stop
    (.stop server))
