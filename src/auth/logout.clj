(ns auth.logout
  (:require
    [mlib.http :refer [json-resp]]
    [mdb.sess :refer [clear-sess]]))


(defn logout [req]
  ;; logout user
  (let [sid (-> req :sess :_id)]
    (clear-sess sid)
    (json-resp {:ok 1 :redir "/"})))
