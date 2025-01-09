(ns app.main
  "angara.main"
  (:gen-class)
  (:require
    [mount.core :refer [start-with-args]]
    [taoensso.telemere :refer [log!]]
    [app.config :as cfg]
    [web.srv]
  ,))
  

(defn -main []
  (log! ["init:" (cfg/build-info)])
  (try
    (-> 
     (cfg/deep-merge (cfg/base-config) (cfg/env-config))
     (start-with-args)
     (as-> $
           (log! ["started:" (str (:started $))])))
    (catch Exception ex
      (log! {:level :warn
             :error ex
             :msg "exception in main"}
            )))
  ,)
