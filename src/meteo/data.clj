(ns meteo.data
  (:require
   [taoensso.telemere :refer [log!]]
   [org.httpkit.client :as http]
   [jsonista.core :as json]
   [clojure.core.memoize :as mem]
   [app.config :refer [conf]]
   ,))


(defn get-json 
  ([url auth]
   (get-json url auth 5000))
  ([url auth timeout]
   (let [hdrs (when auth {"Authorization" auth})
         {:keys [status body error]} @(http/get url {:headers hdrs :timeout timeout})]
     (if (= 200 status) 
       (try
         (json/read-value body json/keyword-keys-object-mapper)
         (catch Exception ex
           (log! :warn ["get-json body parse:" (ex-message ex) url])
           nil))
       (do 
         (if error
           (log! :warn ["get-json error:" (ex-message error) url])
           (log! :warn ["get-json:" status body url]))
         nil
         ,)))))


(defn active-stations-impl []
  (let [cf (-> conf :main :meteo)
        url (str (:meteo-api-url cf) "/active-stations?lat=52.28&lon=104.28&last-hours=30")
        auth (:meteo-api-auth cf)]
    (:stations (get-json url auth))))


(def active-stations
  (mem/ttl active-stations-impl {} :ttl/threshold 120000)) ;; 2 minutes


(comment
  
  (count (active-stations-impl))

  (count (active-stations))

  ,)