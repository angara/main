(ns meteo.data
  (:require
   [taoensso.telemere :refer [log!]]
   [org.httpkit.client :as http]
   [jsonista.core :as json]
   [core.memoize :as mem]
   [app.config :refer [conf]]
   ,))


(set! *warn-on-reflection* true)


(defn safe-json [s]
  (try
    (json/parse-string s true)
    (catch Exception ex
      (log! :warn ["safe-json exception" (ex-message ex)])
      nil)))


(defn active-stations []
  (let [cf (-> conf :main :meteo)
        url (str (:meteo-api-url cf) "/active-stations?last-hours=30")
        ;
        {:keys [status body error]}
        (deref (http/get url {:headers {"Authorization" (:meteo-api-auth cf)} :timeout 5000}))]
    ;
    (if (= 200 status) 
      (-> body (safe-json) :stations)
      (do 
        (if error
          (log! :warn ["meteo-api error:" error])
          (log! :warn ["mateo-api status:" status body])
          )
        nil
        ,))))
 

(def active-stations-cached
  (mem/ttl active-stations {} :ttl/threshold 10000))

