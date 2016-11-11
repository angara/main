
(ns web.sysctl
  (:require
    ; [taoensso.timbre :refer [info warn error]]
    [compojure.core :refer [defroutes ANY]]
    [mount.core :as mount]
    [mlib.conf :refer [conf]]
    [mdb.user :refer [user-by-id sess-new FLDS_REQ_USER]]
    [mlib.web.sess :refer [sid-resp]]))
;


(def ctype-text {"Content-type" "text/plain;charset=utf-8"})
(def ctype-html {"Content-type" "text/html;charset=utf-8"})

(defn rc-text [msg]
  {:status 200 :headers ctype-text :body (str msg)})


(defn check-psw [req handler]
  (let [psw (-> conf :sysctl :psw str)]
    (if (= psw (-> req :params :psw))
      (handler req)
      (rc-text "?psw"))))
;

(defn login [{params :params}]
  (let [uid (:uid params)]
    (if-let [u (user-by-id uid FLDS_REQ_USER)]
      (let [sid (:_id (sess-new {:uid uid :login (:login u)}))]
        (sid-resp (rc-text (str u)) sid))
      (rc-text "!uid"))))
;

(defroutes routes
  (ANY "/login" req (check-psw req login))
  (ANY "/*"     _   (rc-text  (:build conf))))
;

;;.
