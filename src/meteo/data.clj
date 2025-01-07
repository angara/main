(ns meteo.data
  (:require
   [taoensso.telemere :refer [log!]]
   [org.httpkit.client :as http]
   [jsonista.core :as json]
   [clojure.core.memoize :as mem]
   [app.config :refer [conf]]
   ,))


(set! *warn-on-reflection* true)


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
           (log! :warn ["get-json body parse:" url (ex-message ex)])
           nil))
       (do 
         (if error
           (log! :warn ["get-json error:" url error])
           (log! :warn ["get-json:" url status body]))
         nil
         ,)))))


(defn active-stations []
  (let [cf (-> conf :main :meteo)
        url (str (:meteo-api-url cf) "/active-stations?last-hours=30")
        auth (:meteo-api-auth cf)]
    (:stations (get-json url auth))))


(def active-stations-cached
  (mem/ttl active-stations {} :ttl/threshold 10000))


(comment
  
  (active-stations)

  ,)