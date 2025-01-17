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
  (let [build-info (:build-info conf)
        hc (-> conf :main :http)]
    (log! [(:appname build-info) (:version build-info) (:commit build-info)])
    (log! ["start http server:" (:host hc) (:port hc)])
    (run-jetty handler hc)
    ,))


(defstate server
  :start
    (->
      (app-handler)
      (middleware)
      (start))
  :stop
    (.stop server))
