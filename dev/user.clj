(ns user
  (:require
    [mount.core :as mnt]
    [app.config :as cf]
    [web.srv]
   ,))


(set! *warn-on-reflection* true)


(defn start []
  (let [cfg (cf/deep-merge (cf/base-config) (cf/env-config))]
    (-> cfg
        (mnt/with-args)
        (mnt/only #{#'cf/conf})
        (mnt/start)
        )
    )
  )

(comment

  (start)
  (mnt/stop)

  ,)
