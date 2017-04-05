
(ns css.tourserv
  (:require
    [garden.def :refer [defstyles]]
    [garden.units :refer [px pt em ex]]
    [garden.stylesheet :refer [at-media]]))
;

(def b-tourserv
  [:.b-tourserv
    {:margin-bottom (em 1)}])
;

;;.
