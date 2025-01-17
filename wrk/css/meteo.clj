(ns css.meteo
  (:refer-clojure :exclude [>])
  (:require
    [garden.selectors :refer [>]]
    [garden.stylesheet :refer [at-media]]
    ;
    [css.colors :as c]
   ,))


; (def SM_MAX_WIDTH "575.98px")

(def ST_COLOR "#28D")

(def c-pos "#a20")
(def c-neg "#04d")
(def c-zer "#555")


;;; ;;; ;;; ;;; ;;;

(def b-st
  [:.b-st
    [:.title
      {:font-size "3rem"
       :text-align "center"}]
    [:.descr
      {:font-size "2rem"
       :text-align "center"}]
    [:.addr
      {:font-size "2rem"
       :text-align "center"}]
    [:.date
      {:text-align "center"}]
    [:.lbl
      { :text-align "right"
        :width "15ex"
        :margin-right "4px"
        :display "inline-block"}]
    [:.lbl20
      { :width "20ex"}]
    [:.twph
      { :font-size "1.7rem"
        :margin "14px 10px"}
      [:.t
        [:b
          { :margin-left  "1px"
            :margin-right "1px"}]]
      [:.w  [:b { :color "#459"}]]
      [:.h  [:b { :color "#459"}]]
      [:.p  [:b { :color "#459"}]]
      [:.wt [:b { :color "#459"}]]

      [:.arr
        { :position "relative"
          :top "-1px"
          :width "1ex"
          :margin-left "1px"}]]
    [ (> :.dead :div)
      {:color "#777"}]
    [:.dead-msg
      { :text-align "center"
        :color c/c_err
        :margin "6px"
        :font-weight "bold"}]

    [:.months
      [:select
        {:margin "6px"}]
      [:.btn
        {:margin "6px"}]
      [:.btn-curr
        { :font-weight "bold"
          :background-color c/bg_menu_sel
          :color "#fff"}]]
    ;
    [:.st-graph
      { :background-color "#eee"
        :height "340px"
        :border-radius "2px"
        :margin "8px 0 0 0"}
      [:.loading
        { :color "#888"
          :padding "9px 12px"
          :font-size "90%"}]]])
      ; [.t0
      ;   {:color "#555"}]
      ; [.t10
      ;   {:color "#"}]]])

;

;;; ;;; ;;; ;;; ;;;

(def b-card
  [:.b-card
    { :position "relative"
      :border-radius "3px"
      :border (str "1px solid " ST_COLOR)
      :min-height "120px"
      :padding-bottom "1px"
      :margin-bottom "16px"}
    ;
    [:.title
      { :background-color ST_COLOR
        :padding "1px 9px"
        :font-weight "bold"
        :font-size "1.6rem"
        :letter-spacing "0.8px"
        :color "#fff"
        :cursor "pointer"
        :overflow-x "hidden"
        :white-space "nowrap"
        :position "relative"}
      [:.cog
        { :top "2px"
          :right "2px"
          :font-size "1.6rem"
          :position "absolute"
          :z-index 90
          :color "#f4f4a4"
          :cursor "pointer"}]
      [:a :a:visited {:color "#fff"}]
      [:a:hover      {:color "#fcfcaa"}]]
    ;; /title

    [:.nodata
      { :margin "20px"
        :text-align "center"
        :color "#888"}]

    [:.t
      { :color "#333"
        :font-size "26px"
        :white-space "nowrap"
        :float "right"
        :margin "3px 5px 4px 5px"}
      [:i
        { :font-style "inherit"
          :margin-left "2px"
          :margin-right "1px"}]
      ; [:.pos {:color c-pos}]
      ; [:.zer {:color c-zer}]
      ; [:.neg {:color c-neg}]
      [:.arr
        { :position "relative"
          :top "-2px"
          :width "1ex"
          :margin-left "1px"}]]
    ;
    [:.wph
      { :margin "4px 8px 4px 10px"
        :color "#555"}
      [:b {:color "#44b"}]]

    [:.st-descr]

    ;
    [:.graph
      { :background-color "#eee"
        :height "210px"
        :border-radius "2px"
        :margin "8px 0 0 0"}
      [:.loading
        { :color "#888"
          :padding "9px 12px"
          :font-size "90%"}]]
    ;
    [:.i-menu
      { :top "18px"
        :right "8px"
        :font-size "1.6rem"
        :position "absolute"
        :z-index 99}
      ; [:.i-toggle
      ;   { :color "#f4f4a4"
      ;     :cursor "pointer"}]
      [:ul
        {
          :transition "height 1s"
          :list-style "none"
          :margin-top "3px"
          :margin-right "-3px"
          :padding "1px 1px"
          :background-color "#fff"
          :border "1px solid #777"
          :border-radius "1px"
          :box-shadow "1px 1px 6px rgba(0,0,0,0.5)"}
        [:li
          {:cursor "pointer"
            :padding "1px 8px 1px 2px"
            :margin "2px 2px"
            :border-radius "2px"}
          [:i
            {:color "#139"}]
          [:&:hover
            { :background-color c/bg_menu_sel
              :color c/c_menu_sel}]]]]])
;; /b-card

(def b-meteo-brief
  [:.b-meteo-brief
    {:margin-bottom "2em" :margin-top "1ex"}
    [:.station 
      { :font-size "18px" 
        :margin "10px"
        :border-bottom "1px solid #cde"
        :padding-bottom "10px"}

      [:.title  {:font-size "22px" :color "#48e" :margin "0 1em 2px 1em"}
        [:i {:font-size "76%" :margin "0 4px" :color "#383"}]
        [:.hhmm {:font-size "14px" :float "right" :color "#aaa" :margin "4px 0 4px 8px"}]]
      [:.value  {:margin-left "1em" :white-space "nowrap"}]
      [:.val {:text-align "right"}]
      (at-media {:min-width "768px"}
        [:.lbl {:text-align "right"}]     
        [:.val {:text-align "left"}])]])
;

;;; ;;; ;;; ;;; ;;;

(def b-meteo
  [:.b-meteo
    {:margin-top "8px"}

    ;; commons
    [:.pos {:color c-pos}]
    [:.zer {:color c-zer}]
    [:.neg {:color c-neg}]

    [:.selector
      {:margin-top "6px" :margin-bottom "6px"}
      [:select
        {:margin "6px 2px"}]
      [:button
        {:margin "6px 2px"}]]
    ;
    b-meteo-brief
    b-card
    b-st])
  ;; /b-meteo
;.
