;;;;
;;  angara-main
;;;;

(ns web.main
  (:gen-class)
  (:require
    [clojure.string :refer [blank? split]]
    [mount.core :refer [start-with-args defstate]]
    [mlib.core :refer [edn-read edn-resource]]
    [mlib.logger :refer [info warn]]
    [mlib.config :refer [conf]]
    [photomap.core]
    [web.srv]))
;
  
(defn load-env-configs [env]
  (when env
    (->> (split env #"\:")
      (remove blank?)
      (map edn-read))))
;

(defstate main
  :start
    (let [_build (:build conf)]
      (info "main started.")
      true))
;=

(defn -main []
  (info "init...")
  (start-with-args 
    (concat
      [(edn-resource "config.edn") {:build (edn-resource "build.edn")}]
      (load-env-configs (System/getenv "CONFIG_EDN")))))
;;
  
;;.
