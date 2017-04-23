
(ns css.tourserv
  (:require
    [garden.def :refer [defstyles]]
    [garden.units :refer [px pt em ex]]
    [garden.stylesheet :refer [at-media]]
    ;
    [css.colors :refer :all]))
;

(def b-tourserv
  [:.b-tourserv
    {:margin-bottom "1.4rem"}

    [:.town-title
      { :background-color bg_h4
        :border-radius "3px"
        :padding "3px 1.2rem"
        :color "#fff"}]
    [:.b-tserv
      [:.title
        { :font-size "110%"
          :background-color bg_h4
          :border-radius "3px"
          :padding "2px 1.2rem"
          :color "#fff"}]
      [:.descr
        { :font-size "90%"
          :margin "3px 1rem 0 1rem"
          :text-indent "1.2rem;"}]

      [:b {:color c_label_grey}]
      [:i.fa {:color c_icon_blue :margin-right "6px"}]

      [:.payload { :margin "auto 1rem" :color c_icon_darkblue}]
      [:.price   { :margin "auto 1rem" :color c_icon_darkblue}]
      [:.phone   { :margin "auto 1rem" :color c_icon_darkblue}]
      [:.addr    { :margin "auto 1rem" :color c_icon_darkblue}]
      [:.email   { :margin "auto 1rem" :color c_icon_darkblue}]
      [:.person  { :margin "auto 1rem" :color c_icon_darkblue}]]
    [:.b-index]])

;

;;.
