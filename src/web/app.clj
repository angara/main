(ns web.app
  (:require
    [taoensso.telemere :refer [log!]]
    [java-time.api :as jt]
    [tick.core :as t]
    [ring.util.response :refer [redirect]]
    [compojure.core :refer [GET ANY context routes wrap-routes]]
    [compojure.route :as route]
    ;
    [app.config :refer [conf]]
    [mlib.web.sess :refer [wrap-sess]]
    [mlib.web.middleware :refer [middleware]]
    [mdb.user :refer [user-by-id user-update FLDS_REQ_USER]]
    [mdb.sess :refer [sess-load]]
    [html.frame :refer [not-found render-layout]]
    [html.search :refer [ya-site-results]]
    [calendar.core :refer [calendar-routes]]
    [forum.core :refer [forum-api-routes]]
    [front.core :refer [main-page]]
    [auth.core :refer [auth-routes]]
    [me.core :refer [me-routes]]
    [meteo.core :as meteo]
    [tourserv.core :as tourserv]
  ,))


(defn wrap-user-required [handler]
  (fn [req]
    (if (:user req)
      (handler req)
      (redirect
        (str (-> conf :urls :login) "?redir=" (:uri req))))))
;


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
    (context "/auth" _ auth-routes)
    ;
    (GET "/me"  [] (redirect "/me/"))
    (context "/me" [] (wrap-routes me-routes wrap-user-required))
    ;
    ;; was angara/usr module placeholder page
    (GET "/usr"  [] (redirect "/"))
    (GET "/usr/" [] (redirect "/"))
    ;
    (route/resources "/" {:root "public"})
    ;
    (GET "/*" _ not-found)
    (ANY "/*" _ api-404)
    ,))


(def USER_ATIME_INTERVAL (t/of-seconds 10))


(defn update-user-atime [user]
  (try
    (let [atime (:atime user (jt/local-date-time 1970 1 1))]
      (when (t/< atime (t/<< (t/date-time) USER_ATIME_INTERVAL))
        (user-update (:id user) {:atime (t/date-time)})))
    (catch Exception e
      (log! :warn ["update-user-atime:" (:id user) e]))))


(defn wrap-user [handler]
  (fn [req]
    (let [uid (-> req :sess :uid str not-empty)]
      (if-let [user (and uid (user-by-id uid FLDS_REQ_USER))]
        (do
          (update-user-atime user)
          (handler (assoc req :user user)))
        (handler req)))))


(defn wrap-real-ip [handler]
  (fn [req]
    (let [ip (get-in req [:headers "x-real-ip"])]
      (handler (assoc req :real-ip ip)))))


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
    (wrap-real-ip)
    (middleware)
    (wrap-slowreq (:slowreq conf))
   ,))
