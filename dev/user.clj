(ns user
  (:require
    [portal.api :as portal]
    [mount.core :as mnt]
    [app.config :as cf]
    [web.srv]
   ,))


(set! *warn-on-reflection* true)


(defn start []
  (let [cfg (cf/deep-merge (cf/base-config) (cf/env-config))]
    (-> cfg
        (mnt/with-args)
        ;(mnt/only #{#'cf/conf #'cf/tz})
        (mnt/start)
        )
    ))


(comment

  (def p (portal/open {:launcher :vs-code})) ;; NOTE: portal extension required
  (add-tap #'portal/submit)
    
  (portal/clear) ; Clear all values
  
  (prn @p) ; bring selected value back into repl
  
  (remove-tap #'portal/submit) ; Remove portal from tap> targetset
  
  (portal/close) ; Close the inspector when done
  (portal/docs) ; View docs locally via Portal - jvm / node only
  
  (start)
  (mnt/stop)
  
  ()
  )
