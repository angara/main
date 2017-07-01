
(ns css.controls
  (:require
    [garden.units :refer [px pt em ex]]
    [garden.stylesheet :refer [at-media]]
    ;
    [css.colors :refer :all]))
  ;
;

(def c-popmenu
  [
    [:.c-popmenu
      { :position "relative"}]
    ;
    [:.c-popmenu-toggle
      {:cursor "pointer"
       :margin "2px"}]
    ;
    [:.c-popmenu-pane
      { :top "18px"
        :right "8px"
        :font-size "1.6rem"
        :position "absolute"
        :z-index 99
        :transition "all 1s"}
      [:hr
        {:margin "4px 2px"}]
      [:ul
        {
          ; :transition "height 1s"
          :list-style "none"
          :margin-top "3px"
          :margin-right "-3px"
          :padding "1px 1px"
          :background-color "#fff"
          :border "1px solid #777"
          :border-radius "1px"
          :box-shadow "1px 1px 6px rgba(0,0,0,0.5)"}
        [:li
          { :cursor "pointer"
            :text-align 'left
            :padding "1px 8px 1px 8px"
            :margin "2px 3px"
            :border-radius "2px"}
          ; [:i
          ;   {:color "#139"}]
          [:&:hover
            { :background-color bg_menu_sel
              :color c_menu_sel}]]]]])
;

(def controls
  c-popmenu)
;

;;.
