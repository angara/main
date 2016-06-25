
(ns web.sysctl
  (:require
    ; [taoensso.timbre :refer [info warn error]]
    [compojure.core :refer [defroutes ANY]]
    [mount.core :as mount]
    [mlib.conf :refer [conf]]
    [mdb.user :refer [user-by-id sess-new FLDS_REQ_USER]]
    [mlib.web.sess :refer [sid-resp]]))
;


(defn rc-text [msg]
  {:status 200 :headers {"Content-type" "text/plain"} :body msg})


(defn check-psw [req handler]
  (let [psw (-> conf :sysctl :psw str)]
    (if (= psw (-> req :params :psw))
      (handler req)
      (rc-text "?psw"))))
;

(defn login [{params :params}]
  (let [uid (:uid params)]
    (if-let [u (user-by-id uid FLDS_REQ_USER)]
      (let [sid (:_id (sess-new {:uid (:_id u) :login (:login u)}))]
        (sid-resp (rc-text (str u)) sid))
      (rc-text "!uid"))))
;

; (defn exit [{params :params}]
;   (do
;     (mount/stop)
;     (rc-text "system exit")))
; ;

(defroutes routes
  (ANY "/login" req (check-psw req login))
;  (ANY "/exit"  req (check-psw req exit))
  (ANY "/*"     _   (rc-text  "?action")))
;

;;.
