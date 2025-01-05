(ns app.config
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [mount.core :refer [defstate args]]
   ,))


(defn build-info []
  (-> "build-info.edn" (slurp) (edn/read-string)))


(defn- deep-merge* [& maps]
  (let [f (fn [old new]
            (if (and (map? old) (map? new))
              (merge-with deep-merge* old new)
              new))]
    (if (every? map? maps)
      (apply merge-with f maps)
      (last maps))))


(defn deep-merge [& maps]
  (prn maps)
  (let [maps (filter identity maps)]
    (assert (every? map? maps))
    (apply merge-with deep-merge* maps)))


(defn base-config []
  (-> "config.edn" io/resource slurp edn/read-string (assoc :build-info (build-info))))


(defn env-config []
  (-> (System/getenv "CONFIG_EDN") slurp edn/read-string))


(defstate conf
  :start (args))
