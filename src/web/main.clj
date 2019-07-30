;;;;
;;  angara-main
;;;;

(ns web.main
  (:gen-class)
  (:require
    [clojure.string :refer [blank? split]]
    [clojure.edn :as edn]
    [mount.core :refer [start-with-args]]
    [mlib.core :refer [edn-read edn-resource]]
    [mlib.log :refer [info warn]]
    [mlib.conf :refer [conf]]
    [photomap.core]
    [web.srv]))
;
  
(defn load-env-configs [env]
  (when env
    (->> (split env #"\:")
      (remove blank?)
      (map edn-read))))
;

(defn -main [& args]
  (info "init...")
  (start-with-args 
    (concat
      [(edn-resource "config.edn") {:build (edn-resource "build.edn")}]
      (load-env-configs (System/getenv "CONFIG_EDN")))))
;
  
;;.
