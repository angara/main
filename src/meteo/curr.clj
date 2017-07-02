
(ns meteo.curr
  (:require
    [mlib.conf :refer [conf]]
    ;
    [meteo.db :refer [st-by-id]]
    [meteo.fmt :refer [format-t]]
    [meteo.util :refer [st-param fresh]]))
;


;; {:st-id [st ms]}
(defonce cache (atom {}))

(def CACHE_MS 100000) ;; 100 sec


(defn st-cached [st-id]
  (let [now (System/currentTimeMillis)
        [st ms] (get @cache st-id)]
    (if (and st (> ms (- now CACHE_MS)))
      st
      (when-let [st (st-by-id st-id)]
        (swap! cache assoc st-id [st now])
        st))))
;

(defn get-fresh [st-id]
  (when-let [st (st-cached st-id)]
    (when (fresh (:last st))
      st)))
;

(defn curr-temp [req]
  (when-let [st (or
                  (some get-fresh (take 10 (st-param req)))
                  (some get-fresh (-> conf :meteo :st_default)))]
    (when-let [t (format-t
                    (-> st :last :t)
                    (-> st :trends (fresh) :t :avg))]
      [:span.t t])))
;

;;.
