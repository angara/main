
(ns css.calendar
  (:require
    [garden.def :refer [defstyles]]
    [garden.units :refer [px pt em ex]]
    [garden.stylesheet :refer [at-media]]
    ;
    [css.colors :refer :all]))
;

(def b-calendar
  [:.b-calendar
    {:margin "1.4rem auto 1.5rem auto;"}
    [:.b-crec
      { :background-color "#f8f8f8"
        :border-radius "2px"
        :margin "8px 10px"
        :padding "8px"}
      ;
      [:.thumb
        { :float "left"
          :width "100px"
          :height "100px"
          :border-radius "3px"
          :margin-right "4px"
          :margin-left "6px"}]
      ;
      [:.date
        {
          :padding "2px 4px"
          :width "12ex"
          :border "1px solid #aaa"
          :border-radius "2px"
          :text-align :center}]
      ;
      [:.status]
      ;
      [:.title
        {
          :width "100%"
          :margin-top "6px"
          :padding "2px 4px"
          :border "1px solid #aaa"
          :border-radius "2px"}]]])


      ;
;

;;.
