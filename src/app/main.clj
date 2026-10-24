(ns app.main
  "angara.main"
  (:gen-class)
  (:require
    [mount.core :refer [start-with-args]]
    [taoensso.telemere :refer [log!]]
    ;
    [app.config :as cfg]
    [mlib.http.client :as http-client]
    [mlib.tgapi :as tg]
    [web.srv]
    ;; background workers (merged from angara/notify)
    [notify.process.inbound]
    [notify.process.worker]
    [notify.forum.task]
    ,))

(defn -main []
  (log! ["init:" (cfg/build-info)])
  (try
    (let [config (cfg/deep-merge (cfg/base-config) (cfg/env-config))
          proxy-url (some-> config :notify :telegram :http-proxy)]
      (tg/set-http-client! (http-client/make-http-client {:proxy-url proxy-url}))
      (->
       config
       (start-with-args)
       (as-> $
             (log! ["started:" (str (:started $))]))))
    (catch Exception ex
      (log! {:level :warn
             :error ex
             :msg "exception in main"}
            )))
  ,)
