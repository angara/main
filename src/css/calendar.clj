
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
        :border-radius "3px"
        :margin "10px 10px"
        :padding "8px"}
      ;
      [:.thumb
        { :float "left"
          :width "100px"
          :height "100px"
          :border-radius "3px"
          :margin-right "0"
          :margin-left "4px"
          :background-color "#e0e0e0"}]
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
      [:.lbl-status
        { :margin-left "10px" :color "#555"
          :font-weight "normal" :cursor "pointer"}]
      ;
      [:.title
        {
          :width "100%"
          :margin-top "6px"
          :padding "2px 4px"
          :border "1px solid #aaa"
          :border-radius "2px"}]

      [:.c_publ
        { :color c_green
          :font-weight "bold"}]
      [:.c_none
        {:color c_grey}]]])

      ;
;

;;.
