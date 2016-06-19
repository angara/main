
(ns web.srv
  (:require
    ; [clojure.java.io :as io]
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
    ; (let [stop-uri
    ;         (-> conf :http :stop-uri)
    ;       wrap-stop
    ;         (fn [handler]
    ;           #(if (= stop-uri (:uri %))
    ;             (mount/stop)
    ;             (handler %)))]
      (->
        (app/app-handler)
;        (wrap-stop)
        (middleware)
        start)
  :stop
    (.stop server))
;

;;.
