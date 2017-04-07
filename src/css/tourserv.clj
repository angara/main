
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
    {:margin-bottom (em 1)}

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

      [:b {:color c_label_green}]
      [:i.fa {:color c_label_green}]

      [:.addr  { :margin "auto 1rem"}]
      [:.price { :margin "auto 1rem"}]
      [:.phone { :margin "auto 1rem"}]
      [:.email { :margin "auto 1rem"}]]])
;

;;.
