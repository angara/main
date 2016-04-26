
(ns mlib.conf
  (:require
    [mount.core :refer [defstate]]
    [mlib.core :refer [deep-merge edn-read edn-resource]]))
;

(defonce run-conf (atom {}))

(defstate conf
  :start (deep-merge
            (edn-resource "config.edn")
            {:build (edn-resource "build.edn")}
            @run-conf))
;

;.
