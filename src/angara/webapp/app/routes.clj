(ns angara.webapp.app.routes
  (:require
   [reitit.ring :as ring]
   [reitit.middleware :as middleware]
   ;
   [angara.webapp.app.middleware :as mware :refer [wrap-request-config wrap-error-page]]
   [angara.webapp.forum.handlers :refer [h-topic h-group h-group-list]]
   ,))


(defn webapp-routes []
  [,
   ["/forum" {:middleware [:config :error-page]}
    ["/groups"          {:get h-group-list}]
    ["/group/:group_id" {:get h-group}]
    ["/topic/:topic_id" {:get h-topic}]]
   ,])


;; /webapp/assets/* static is served by the main route/resources
;; from resources/public/webapp/assets (same URIs)

(defn make-handler []
  (-> (webapp-routes)
      (ring/router
        {::middleware/registry
         {:config     wrap-request-config
          :error-page wrap-error-page}})
      (ring/ring-handler)))
