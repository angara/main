
(ns css.meteo
  (:require
    [garden.units :refer [px pt em ex]]
    [garden.stylesheet :refer [at-media]]
    ;
    [css.colors :refer :all]))
;


(def ST_COLOR "#28D")

(def b-meteo
  [:.b-meteo
    {:margin-top "8px"}

    [:.b-card
      { :border-radius "3px"
        ; :box-shadow
        ;   (str  "0 0 1px rgba(255,255,255,0.9),"
        ;         "1px 1px 6px rgba(0,0,0,0.4)")

        :border (str "1px solid " ST_COLOR)
        :min-height "120px"
        :margin-bottom "16px"}


      [:.title
        { :background-color ST_COLOR
          :padding "1px 9px"
          :font-weight "bold"
          :font-size "110%"
          :letter-spacing "0.8px"
          :color "#fff"
          :overflow-x "hidden"
          :white-space "nowrap"
          :position "relative"}]

      [:.nodata
        { :margin "20px"
          :text-align "center"
          :color "#888"}]

      [:.t
        { :color "#333"
          :font-size "20px"
          :white-space "nowrap"
          :float "right"
          :margin "4px 5px 4px 5px"}
        [:i
          { :font-style "inherit"
            :margin-left "2px"
            :margin-right "1px"}]
        [:.pos {:color "#a20"}]
        [:.zer {:color "#555"}]
        [:.neg {:color "#04d"}]
        [:.arr
          { :position "relative"
            :top "-2px"
            :width "1ex"
            :margin-left "1px"}]]
            ;:font-size "15px"}]]
      ;
      [:.wph
        { :margin "4px 8px 4px 8px"
          :color "#555"}
        [:b {:color "#44b"}]]

      [:.st-descr]

      [:&:hover
        {:box-shadow "0 0 8px rgba(0,80,200,0.8)"}]]

    ;; /b-card

    [:.selector
      {:margin-top "6px" :margin-bottom "6px"}]])


  ;; /b-meteo
;.
