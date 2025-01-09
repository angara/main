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
         {:keys [status body error]} 
         #_{:clj-kondo/ignore [:unresolved-var]}
         (deref (http/get url {:headers hdrs :timeout timeout}))]
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


(defn st-info-impl [st]
  (when st
    (let [cf (-> conf :main :meteo)
          url (str (:meteo-api-url cf) "/station-info?st=" st)
          auth (:meteo-api-auth cf)]
      (get-json url auth))))


(def st-info 
  (mem/ttl st-info-impl {} :ttl/threshold 20000))  ;; 20 sec


(defn st-hourly-impl [st ts-beg ts-end]
  (when st
    (let [cf (-> conf :main :meteo)
          url (str (:meteo-api-url cf) 
                   "/station-hourly?st=" st "&ts-beg=" ts-beg "&ts-end=" ts-end)
          auth (:meteo-api-auth cf)]
      (get-json url auth))))


(def st-hourly
  (mem/ttl st-hourly-impl {} :ttl/threshold 80000))  ;; 80 sec


(comment
  
  (count (active-stations-impl))
  (count (active-stations))

  (st-info "uiii")
  ;;=> {:closed_at nil,
  ;;    :publ true,
  ;;    :last_ts "2025-01-09T19:00:00+08:00",
  ;;    :elev 495.0,
  ;;    :title "Иркутский аэропорт",
  ;;    :note nil,
  ;;    :st "uiii",
  ;;    :lon 104.366972,
  ;;    :lat 52.267288,
  ;;    :descr "г. Иркутск, ул. Ширямова, 101",
  ;;    :last
  ;;    {:g_delta 3.0,
  ;;     :w_ts "2025-01-09T19:00:00+08:00",
  ;;     :w 4.0,
  ;;     :d_delta 0.5,
  ;;     :p_ts "2025-01-09T19:00:00+08:00",
  ;;     :t_delta 0.0,
  ;;     :g 14.0,
  ;;     :b_ts "2025-01-09T19:00:00+08:00",
  ;;     :g_ts "2024-12-29T09:45:00+08:00",
  ;;     :d_ts "2025-01-09T19:00:00+08:00",
  ;;     :b 310.0,
  ;;     :t_ts "2025-01-09T19:00:00+08:00",
  ;;     :d -14.0,
  ;;     :t -11.0,
  ;;     :w_delta 0.0,
  ;;     :p 975.0,
  ;;     :p_delta 0.0},
  ;;    :created_at "2004-10-10T21:00:00+09:00"}
  
  (require '[java-time.api :as jt])
  (let [st "uiii"
        ts-end (jt/instant)
        ts-beg (jt/truncate-to (jt/minus ts-end (jt/hours 10)) :hours)]
    (st-hourly st ts-beg ts-end)
    )
  ;;=> {:ts-beg "2025-01-09T01:00:00Z",
  ;;    :series
  ;;    {:w [2.5 0.5 1.0 1.5 1.0 0.0 1.5 2.5 4.0 4.0 4.0],
  ;;     :d [-20.0 -21.5 -20.0 -17.5 -15.5 -16.5 -14.5 -13.25 -14.0 -14.5 -14.0],
  ;;     :t [-18.0 -19.5 -18.0 -14.5 -10.0 -9.5 -9.5 -9.75 -11.0 -11.0 -11.0],
  ;;     :p [975.0 975.5 975.5 975.0 975.5 975.0 976.0 976.0 974.0 975.0 975.5]},
  ;;    :ts-end "2025-01-09T11:00:00Z",
  ;;    :st "uiii"}
  (let [st "npsd"
        ts-end (jt/instant)
        ts-beg (jt/minus ts-end (jt/hours 10))]
    (st-hourly st ts-beg ts-end))
  ;;=> {:ts-beg "2025-01-09T01:00:00Z",
  ;;    :series
  ;;    {:t
  ;;     [-14.0
  ;;      -11.633333333333333
  ;;      -10.124999999999998
  ;;      -9.641666666666667
  ;;      -9.016666666666666
  ;;      -9.008333333333333
  ;;      -8.258333333333333
  ;;      -9.566666666666666
  ;;      -11.258333333333331
  ;;      -11.016666666666666
  ;;      -11.3625],
  ;;     :p
  ;;     [969.5137539605
  ;;      969.7848428201665
  ;;      969.8092852583333
  ;;      969.8437268757499
  ;;      969.9092770508331
  ;;      970.0648198391664
  ;;      970.4703421087498
  ;;      971.040295326
  ;;      972.0779879281664
  ;;      972.9490275428333
  ;;      973.6117509231249]},
  ;;    :ts-end "2025-01-09T11:00:00Z",
  ;;    :st "npsd"}
  
    (let [st "npsd"
          ts-end (jt/instant)
          ts-beg (jt/truncate-to (jt/minus ts-end (jt/hours 0)) :hours)]
      (st-hourly st ts-beg ts-end))
    ;;=> {:ts-beg "2025-01-09T12:00:00Z",
    ;;    :series {:t [-11.875], :p [974.0633805192499]},
    ;;    :ts-end "2025-01-09T12:00:00Z",
    ;;    :st "npsd"}

    (jt/time-between (jt/instant) (jt/truncate-to (jt/instant) :hours) :minutes)

  ()
  )