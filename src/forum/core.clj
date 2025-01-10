(ns forum.core
  (:require
    [compojure.core :refer [defroutes context ANY]]
    [mlib.http :refer [json-resp]]
    [forum.topic :refer [topic-routes]]))


(defroutes forum-api-routes
  (context  "/topic"  _ topic-routes)
  (ANY      "/*"      _ (json-resp {:err :req})))
