
(ns css.meteo
  (:require
    [garden.units :refer [px pt em ex]]
    [garden.stylesheet :refer [at-media]]
    ;
    [css.colors :refer :all]))
;


(def b-meteo
  [:.b-meteo
    {:margin-top "8px"}

    [:.b-card
      { :border-radius "5px"
        :box-shadow
          (str  "0 0 1px rgba(255,255,255,0.9),"
                "1px 1px 6px rgba(0,0,0,0.4)")
        :padding "8px"
        :min-height "120px"}


      [:.st-name]

      [:.st-descr]

      [:&:hover
        {:box-shadow "0 0 10px rgba(0,80,200,0.8)"}]]])

    ;; /b-card

  ;; /b-meteo
;.
