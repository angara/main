(ns web.app
  (:require
    [taoensso.telemere :refer [log!]]
    [ring.util.response :refer [redirect]]
    [compojure.core :refer [GET ANY context routes]]
    [compojure.route :as route]
    ;
    [app.config :refer [conf]]
    [mlib.web.sess :refer [wrap-sess]] ; sid-resp]]
    [mlib.web.middleware :refer [middleware]]
    [mdb.user :refer [user-by-id sess-load FLDS_REQ_USER]]
    [html.frame :refer [not-found render-layout]]
    [html.search :refer [ya-site-results]]
    [calendar.core :refer [calendar-routes]]
    [forum.core :refer [forum-api-routes]]
    [front.core :refer [main-page]]
    [meteo.core :as meteo]
    [tourserv.core :as tourserv]
  ,))


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
    (GET "/"                    _ main-page) ;; draft!
    ;
    (GET "/meteo/"              [] meteo/index-page)
    (GET "/meteo/st/:st"        [] meteo/st-page)
    (GET "/meteo/st-hourly/:st" [] meteo/st-hourly)
    ;
    (GET "/search"    _ (redirect "/yasearch/"))
    (GET "/yasearch/" _ ya-search)
    ;
    (context "/calendar"  _ calendar-routes)
    (context "/forum/api" _ forum-api-routes)
    (context "/tourserv"  _ tourserv/routes)
    ;
    (route/resources "/" {:root "public"})
    ;
    (GET "/*" _ not-found)
    (ANY "/*" _ api-404)
    ,))


(defn wrap-user [handler]
  (fn [req]
    (if-let [uid (-> req :sess :uid str not-empty)]
      (handler (assoc req :user
                  (user-by-id uid FLDS_REQ_USER)))
      (handler req))))


(defn wrap-slowreq [handler cnf]
  (let [ms (:ms cnf)]
    (fn [req]
      (let [t0    (System/currentTimeMillis)
            resp  (handler (assoc req :start-time t0))
            dt    (- (System/currentTimeMillis) t0)]
        (when (< ms dt)
          (log! {:msg "slow request"
                 :data (-> req
                           (select-keys [:remote-addr :uri])
                           (assoc :duration dt))}))
        resp))
    ,))


(defn app-handler []
  (->
    (make-routes)
    (wrap-user)
    (wrap-sess sess-load)
    (middleware)
    (wrap-slowreq (:slowreq conf))
   ,))
