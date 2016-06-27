;;;;
;;  angara-main
;;;;

(ns web.app
  (:require
    [taoensso.timbre :refer [info warn error]]
    ; [mount.core :refer [defstate] :as mount]

    ; [org.httpkit.server :refer [run-server]]

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
    [front.core :refer [main-page]]
    [web.sysctl :as sysctl]))

    ; [usr.bb.handler :as bb-handler]
    ; [usr.pb.handler :as pb-h]
    ; [usr.bb.categ :as bb-categ]
    ; [usr.bb.model :as bb-model]
    ; [usr.bb.tags :as bb-tags]
    ; [usr.html.views :as views]
    ; [usr.beta :as beta]))
;



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
    (GET  "/"       _  main-page)
    ; (ANY  "/search" _  views/search)
    (ANY  "/yasearch" _  search/yasearch)

    (context (-> conf :sysctl :prefix) _ sysctl/routes)

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
  ; (GET  "/bb" [] (redirect "/bb/"))
  ; (context "/bb" [] bb-handler/routes)
  ;
  ; (GET  "/pb" [] (redirect "/pb/"))
  ; (context "/pb" [] pb-h/routes)

  ; (context "/beta" [] beta/routes)

  ;; /my

  ; (GET  "/loc/:loc" [loc :as req] (sv/loc req loc))

  ; (GET  "/story/:id" [id :as req] (sv/story req id))
  ; (POST "/story/new" [:as req] (user-req sv/story-new req))

  ; (GET  "/story/:id/edit" [id :as req] (user-req sv/story-edit req id))
  ; (POST "/story/:id/edit" [id :as req] (user-req sv/story-update req id))

;  (route/files "/usr/inc" {:root "inc"})
  ; (route/resources "/")

;

(defn wrap-user [handler]
  (fn [req]
    (let [uid (-> req :sess :uid str)
          user (user-by-id uid FLDS_REQ_USER)]
      (handler (assoc req :user user)))))
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
