
(ns css.calendar
  (:require
    [css.colors :refer [c_green c_grey bg_dark]]
   ,))


(def b-calendar
  [:.b-calendar
    {:margin "1.5rem auto 1.5rem auto;"}
    ;;

    ; [:.row {:margin-bottom "10px"}]

    [:.thumb
      { :float "left"
        :width "100px"
        :height "100px"
        :border-radius "3px"
        :background-color "#e0e0e0"}]
    ;;

    [:.b-crec
      { :background-color "#f8f8f8"
        :border-radius "3px"
        :margin "10px 10px"
        :padding "8px"}
      ;
      [:.thumb
        { :margin-right "0"
          :margin-left "4px"}]
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
        {:color c_grey}]]

    ;;; ;;; ;;; ;;;

    [:.b-card
      {
        :min-height "150px"
        :margin-top "14px"
        :margin-bottom "14px"
        :padding "6px 12px"
        :border "1px solid #dde"
        :border-radius "3px"
        :background-color "#f8f8f8"}

      [:.date
        {
          :font-size "120%"
          :color bg_dark
          :margin "1px 2px 2px 0"}]

      [:.thumb
        {:margin "0 10px 8px 0"}]
      ;
      [:&:hover
        {:background-color "#ffffee"}]]]
        ,)
