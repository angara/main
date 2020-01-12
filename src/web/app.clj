;;;;
;;  angara-main
;;;;

(ns web.app
  (:require
    [mlib.logger :refer [debug info warn]]
    [ring.util.response :refer [redirect]]
    [compojure.core :refer [GET ANY context routes]]
    [compojure.route :as route]

    ; [monger.collection :as mc]
    ; [monger.operators :refer [$set $unset]]

    [mlib.config :refer [conf]]
;    [mlib.http :refer [json-resp text-resp]]
    [mlib.time :refer [now-ms]]
    [mlib.web.sess :refer [wrap-sess]] ; sid-resp]]
    [mlib.web.middleware :refer [middleware]]

    [mdb.user :refer [user-by-id sess-load FLDS_REQ_USER]]
    [html.frame :refer [not-found render-layout]]
    [html.search :refer [ya-site-results]]
    [calendar.core :refer [calendar-routes]]
    [forum.core :refer [forum-api-routes]]
    [front.core :refer [main-page]]
    [meteo.api :refer [meteo-api-routes]]
    [meteo.core :refer [meteo-routes]]
    [meteo.old-ws :as old-ws]
    [misc.icestorm :as icestorm]
    [photomap.core :as photomap]
    [tourserv.core :as tourserv]))
;

; (defn wrap-user-required [handler]
;   (fn [req]
;     (if (:user req)
;       (handler req)
;       (redirect
;         (str (-> conf :urls :login) "?redir=" (:uri req))))))
; ;


(defn api-404 [_req]
  { :status  404
    :headers {"Content-Type" "text/plain"}
    :body    "API endpoint not found"})
;

(defn ya-search [req]
  (render-layout req
    { :title "Яндекс Поиск"
      :page-title "Поиск по сайту"
      :rtb-top    (-> conf :rtb-top)
      :rtb-bottom (-> conf :rtb-bottom)}
    (ya-site-results)))
;

(defn make-routes []
  (routes
    (GET     "/"              _ main-page)
    ;
    (context "/api/meteo"     _ meteo-api-routes)
    (ANY     "/api/*"         _ api-404)
    ;
    (context "/meteo"         _ meteo-routes)
    ;
    (GET     "/search"        _ (redirect "/yasearch/"))
    ;; (GET     "/yasearch"      _ search/yasearch)
    (GET     "/yasearch/"     _ ya-search)

    (context "/calendar"      _ calendar-routes)
    (context "/forum/api"     _ forum-api-routes)
    (context "/meteo/old-ws"  _ old-ws/routes)
    (context "/icestorm"      _ icestorm/routes)
    (context "/tourserv"      _ tourserv/routes)
    (context "/photomap"      _ (photomap/make-routes))

    (route/resources "/" {:root "public"})

    (GET "/*" _  not-found)
    (ANY "/*" _  api-404)))
;


(defn wrap-user [handler]
  (fn [req]
    (if-let [uid (-> req :sess :uid str not-empty)]
      (handler (assoc req :user
                  (user-by-id uid FLDS_REQ_USER)))
      (handler req))))
;


(defn wrap-slowreq [handler cnf]
  (let [ms (:ms cnf)]
    (fn [req]
      (let [t0    (now-ms)
            resp  (handler (assoc req :start-time t0))
            dt    (- (now-ms) t0)]
        (when (< ms dt)
          (info "slowreq:" dt (:remote-addr req)(:uri req)))
        resp))))
;

(defn app-handler []
  (->
    (make-routes)
    (wrap-user)
    (wrap-sess sess-load)
    middleware
    (wrap-slowreq (:slowreq conf))))
;

;;.
