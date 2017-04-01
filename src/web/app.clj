;;;;
;;  angara-main
;;;;

(ns web.app
  (:require
    [mlib.log :refer [debug info warn]]
    [ring.util.response :refer [redirect]]
    [compojure.core :refer [GET POST ANY context routes]]
    [compojure.route :as route]

    [monger.collection :as mc]
    [monger.operators :refer [$set $unset]]

    [mlib.conf :refer [conf]]
    [mlib.http :refer [json-resp text-resp]]
    [mlib.time :refer [now-ms]]
    [mlib.web.sess :refer [wrap-sess sid-resp]]
    [mlib.web.middleware :refer [middleware]]

    [mdb.user :refer [user-by-id sess-load FLDS_REQ_USER]]
    [html.frame :refer [not-found]]
    [html.views :as views]
    [html.search :as search]
    [forum.api :as forum-api]
    [front.core :refer [main-page]]
    [meteo.old-ws :as old-ws]
    [misc.icestorm :as icestorm]
    [photomap.core :as photomap]))
;

(defn wrap-user-required [handler]
  (fn [req]
    (if (:user req)
      (handler req)
      (redirect (str "/user/login?next=" (:uri req))))))
;

; (defn logout [req]
;   ;; logout user
;   (sess-update (-> req :sess :sid)
;     {:uid nil :auth nil :login nil :logout (-> req :user :_id)})
;   (json-resp {:ok 1 :redir "/"}))
; ;
;


(defn make-routes []
  (routes
    (GET  "/"         _ main-page)
    (GET  "/search"   _ (redirect "/yasearch"))
    (GET  "/yasearch" _ search/yasearch)

    (context "/forum/api"     _ forum-api/routes)
    (context "/meteo/old-ws"  _ old-ws/routes)
    (context "/icestorm"      _ icestorm/routes)
    (context "/photomap"      _ (photomap/make-routes))

    (if (:dev conf)
      (route/files "/" {:root "tmp/res/public"})
      (route/resources "/" {:root "public"}))

    (ANY "/*" _  not-found)))
;

  ; /txt/

  ; (GET  "/usr/" [] views/main-page)       ;; !!!
  ;
  ; (GET  "/me" [] (redirect "/me/"))
  ; (context "/me" [] (wrap-user-required me-routes))
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
      (let [t0 (do (now-ms))
            resp (do (handler (assoc req :start-time t0)))
            dt (do (- (now-ms) t0))]
        (when (< ms dt)
          (info "slowreq:" dt (:remote-addr req)(:uri req)))
        resp))))
;

(defn app-handler []
  (->
    (make-routes)
    (wrap-user)
    ;; TODO: fix
    ;; (wrap-csrf {:skip-uris #{"/usr/login"}})
    (wrap-sess sess-load)
    middleware
    (wrap-slowreq (:slowreq conf))))

;;.
