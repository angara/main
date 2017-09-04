
(ns css.root
  (:refer-clojure :exclude [>])
  (:require
    [garden.def :refer [defstyles]]
    [garden.units :refer [px pt em ex]]
    [garden.selectors :refer [>]]
    [garden.stylesheet :refer [at-media]]
    ;
    [css.controls :refer [controls]]
    [css.calendar :refer [b-calendar]]
    [css.meteo :refer [b-meteo]]
    [css.tourserv :refer [b-tourserv]]))
;

(def p100 "100%")

(def fa "#fafafa")
(def f8 "#f8f8f8")
(def f4 "#f4f4f4")
(def bd "#bdbdbd")
(def ccc "#ccc")
(def ddd "#ddd")

(def gcurr "#6af")

(def hdr-brd "#039be5")

(def c_a          "#18d")
(def c_av         "#18e")
(def c_ah         "#09f")
(def c_aa         "#a80")
(def c_em         "#E65100")
(def c_dlg_title  "#3949AB")

(def c_topbar_brd "#039BE5")      ; light blue 600


;;; ;;; ;;; ;;;


(def abs-pos
  {:position :absolute :top 0 :left 0 :bottom 0 :right 0})
;

(defn brd1 [place color]
  {(str "border-" (name place)) (str "1px solid " color)})
;


(def commons
  [
    [:body
      { :font-family "\"Open Sans\",\"Helvetica Neue\",Helvetica,Arial,Verdana,sans-serif"
        :font-size "14px"
        :line-height "1.3"}]

    [:a
      {:text-decoration 'none :color "#18d"}]
    [:a:visited
      {:text-decoration 'none :color "#18e"}]
    [:a:hover
      {:text-decoration 'underline :color "#09f"}]
    [:a:active
      {:text-decoration 'underline :color "#a80"}]

    [:p.txt {:text-indent (em 2)}]

    [:.flex-mid
      { :display 'flex
        :flex-direction 'column
        :justify-content 'center}]
    [:.flex-bot
      { :display 'flex
        :flex-direction 'column
        :justify-content 'flex-end}]

    [:.msg
      { :color "#888"
        :padding (px 2)}]
    [:.msg_ok
      { :color "#090"
        :font-weight "bold"}]
    [:.msg_err
      { :color "#a00"
        :font-weight "bold"}]])
;

(def margins
  [
    [:.amar {:margin-left 'auto :margin-right 'auto}]

    [:.marl-4 {:margin-left "4px"}]
    [:.marl-6 {:margin-left "6px"}]
    [:.marl-8 {:margin-left "8px"}]

    [:.marr-4 {:margin-right "4px"}]
    [:.marr-6 {:margin-right "6px"}]
    [:.marr-8 {:margin-right "8px"}]])
;

(def topbar
  [
    [:.b-topbar
      { :margin-bottom (px 4)
        :border-bottom (str "1px solid " c_topbar_brd)}

      [:.logo
        { :margin "4px 4px 3px 4px"}]

      [:.b-search
        {:margin "12px 0"}]

      [:.b-signin
        { :margin "15px 2px 15px 8px"
          :text-align 'right}]

      [:.b-user
        { :text-align 'right
          :margin "11px 2px 12px 8px"}
        [:.name
          { :overflow 'hidden
            :white-space 'nowrap}]]
        ;

        ; [:.upic
        ;   { :display 'block
        ;     :float 'right
        ;     :width (px 40)
        ;     :height (px 40)
        ;     :border-radius "10px"
        ;     :background "#f4f4f4"}]

      (at-media {:max-width (px 768)}
        [:.b-search {:margin "4px 0"}]
        [:.b-signin {:margin "2px 0 12px 8px"}]
        [:.b-user   {:margin "2px 0 12px 8px"}])

      (at-media {:max-width (px 360)}
        [:.logo {:margin-left "-10px"}])]

    ;;;

    [:.b-topnav
      { :font-size "1.6rem"
        :letter-spacing "0.1rem"
        :padding-bottom "4px"
        :margin-bottom "10px"}
      ;
      [:.menu
        { :margin "2px 2px"
          :padding "0"}
        [:li
          { :display "block"
            :float "left"
            :white-space "nowrap"
            :margin-bottom "8px"}
          [:a
            { :padding "4px 2px"
              :transition "0.4s ease"
              :color "#47c"
              :position "relative"
              :text-decoration "none"
              :white-space "nowrap"
              :margin-right "12px"
              :border-bottom "3px solid transparent"}]
          [:.t
            { :font-size "1.4rem"
              :margin-left "-7px"}
            [:b {:font-weight "normal"}]
            [:.pos {:color "#a20"}]
            [:.zer {:color "#555"}]
            [:.neg {:color "#04d"}]]]


        [:li.active
          [:a
            { :border-bottom-color "#28f"
              :color "#002"}]]

        [:li
          [:a:hover
            { :color "#49e"
              :border-bottom-color "#e00"}]]]]])

;

;;; ;;; ;;; ;;;

(def botnav
  [:.b-botnav {:text-align 'center :margin "16px auto 8px auto"}])
;

(def footer
  [:.b-footer
    (merge
      (brd1 :top c_topbar_brd)
      { :margin-top (px 8)
        :padding "1px 0 0 0"})

    [:.footer-bg
      { :padding "1.3ex 2.4ex 1.4ex 2.4ex"
        :background-color "#f4f4f4"
        :border-bottom-left-radius "4px"
        :border-bottom-right-radius "4px"}
      [:.copy
        {:color "#777"}
        [:.brand {:margin-left "2 px" :margin-right "1px"}]]]])
;


(def content
  [:.content
    [:.page-title
      { :margin-top "8px"
        :letter-spacing "0.12ex"
        :text-align 'center
        :color "#48c"}]])

(defstyles main
  commons
  margins
  controls

  content

  topbar
  botnav
  footer

  [:.yandex-adaptive
    {:width "940px" :height "120px" :margin "0 auto"}]
  (at-media {:max-width (px 768)}
    [:.yandex-adaptive
      {:width "600px" :height "120px"}])
  (at-media {:max-width (px 576)}
    [:.yandex-adaptive
      {:width "290px" :height "250px"}])

  [:.rtb-top {:margin-top "4px"}]
  [:.rtb-bottom {:margin-top "10px" :margin-bottom "4px"}]

  b-calendar
  b-meteo
  b-tourserv)
;

;;.


; // c_dark  = #3a609e
; // c_norm  = #5b86c9
; // c_light = #e6edf8
; // c_grey  = #557
; // c_lgrey = #f4f4f4
;
; // bg_f0 = #f0f0f0
; // bg_f4 = #f4f4f4
;
; // bg_light = #f0f0f0
; // bg_dark  = #5c6b81
;
; // c_brd = #4af
; // c_da  = #dadada
;
; // bg_input = #fcfcf0
;
;
; c1 = #00305a
; c2 = #004b8d
; c3 = #0074d9
; c4 = #4192d9
; c5 = #7abaf2
;
; c1 = #072040
; c2 = #163e73
; c3 = #367fbf
; c4 = #5eadf2
; c5 = #94cef2
;
; // flatui
; c1 = #2c3e50
; c2 = #e74c3c
; c3 = #ecf0f1
; c4 = #3498db
; c5 = #2980b9
;
; // bluesky
; c1 = #16193b
; c2 = #35478c
; c3 = #4e7ac7
; c4 = #7fb2f0
; c5 = #add5f7
;
; // orange
; c_or = #ff712c
;
; // planet
; c1 = #00305a
; c2 = #004b8c
; c3 = #0074d9
; c4 = #4192d9
; c5 = #7abaf2
;
; //
; // c1 = #334
; // c2 = #567
; // c3 = #89a
; // c4 = #bcd
; // c5 = #def
;
; c_bbtitle = #3949AB
;


; .fsize-90
;     font-size: 90%
;
; .btn-spin
;     position: relative
;     .spinner
;         display: none
;         position: absolute
;         top: 50%
;         margin-top: -10px
;         right: -28px
;
; .btn
;     position: relative
;
; .btn_spinner
;     position: absolute
;     top: 1px
;     left: 0
;     right: 0
;     bottom: 1px
;     text-align: center
;     opacity: 0.8
;     color: #000
;     font-size: 150%
;
;
; .upic
;     width: 44px
;     height: 44px
;     margin: 0 2px 0 2px
;     padding: 1px
;     border-radius: 3px
;     border: 1px solid #dadada
;
;
; .scroll_to_top
;     position: fixed
;     left: 38px
;     bottom: 38px
;     visibility: hidden
;     opacity: 0
;     transition: all 0.5s ease-in-out;
;     z-index: 9999
;     color: rgba(180,180,220,0.7)
;     font-size: 40px
;     padding: 1px 4px
;     border-radius: 10px
;
; .scroll_to_top .fa
;     display: block
;
; .scroll_to_top:hover
;     background-color: #aaa
;     color: #eef
;
; .scroll_to_top.show
;     visibility: visible
;     cursor: pointer
;     opacity: 1.0
;
; // magnific-popup
;
; .mfp-zoom-out-cur .mfp-image-holder button.mfp-close
;     cursor: auto
;
;
;
; m-square()
;     .sq-wrap
;         width: 100%
;         padding-bottom: 100%
;         position: relative
;         height: 0
;     .sq-frame
;         position: absolute
;         overflow: hidden
;         left: 0
;         right: 0
;         top: 0
;         bottom: 0
;     .sq-pic
;         width: 100%
;         display: block
;         border-radius: 4px
;
;
; m-tag()
;     cursor: pointer
;     display: inline-block
;     background-color: #ece4ec
;     border-radius: 3px
;     margin: 2px 4px
;     padding: 1px 8px
;     letter-spacing: 0.6px
;     transition: color ease-in-out .2s
;     color: #444
;
;
; .page
;     .page-title
;         font-family: Arial,sans-serif
;         font-size: 32px
;         text-align: center
;         color: #3F51B5
;
;     .control-label
;         font-family: Verdana,Arial,sans-serif
;         color: #607D8B
;
;     .bb-nav
;         background-color: #f4f4f4
;         padding: 8px 2px
;         border-radius: 6px
;
;         .item
;             display: inline-block
;             padding: 4px 8px
;             border-radius: 6px
;             margin-left: 8px
;
;             .glyphicon
;                 margin-right: 6px
;
;         .item:hover
;             background-color: #fff
;
;
;     .btn_bb-add
;         text-shadow: 0 0 2px rgba(0,0,0,0.7)
;
;
; .bb-home
;     .top10-item
;         line-height: 120%
;         margin-bottom: 3px
;         padding-left: 1.5em
;         text-indent: -1.5em
;
;     .categ
;         margin-top: 10px
;         .title
;             font-size: 18px
;             background-color: #f4f4f4
;             padding: 2px 10px
;             border-radius: 3px
;         .rels
;             margin: 3px
;             padding-bottom: 3px
;             border-bottom: 1px dashed #78909C
;         .rel>a
;             m-tag()
;             color: #555
;         .rel>a:hover
;             color: #68f
;
;
; .b-taglist
;     margin: 8px
;
;     .bbcat
;         border-radius: 10px
;         border: 1px solid #ddd
;         overflow: hidden
;         margin-top: 12px
;         margin-bottom: 12px
;
;         .title
;             font-weight: bold
;             background-color: #f4f4f4
;             padding: 3px 12px
;             color: #6A1B9A
;         .tags
;             margin: 6px 10px
;
;     .tag
;         m-tag()
;     .tag-wrap
;         display: inline-block
;         margin: 8px
;
;
;
; .bb-pane
;     // margin: 10px -10px 14px -10px       // .row margins -4px
;     margin: 10px auto
;
;     .bb-list
;         border-radius: 4px
;         background-color: #f7f7f7
;         padding: 1px 6px
;
;         .item
;             background-color: #fff
;             padding: 6px 6px
;             border-radius: 4px
;             margin-bottom: 6px
;             margin-top: 6px
;             position: relative
;             overflow: hidden
;
;             m-square()
;
;             .sq-pic
;                 transition: color ease-in-out .3s
;             .sq-pic:hover
;                 box-shadow: 0 0 6px rgba(41,182,246 ,1)
;
;             .item-text
;                 padding-bottom: 18px
;                 position: relative
;
;             .title
;                 margin-left: 2px
;                 font-size: 16px
;                 font-weight: bold
;                 color: #557
;                 display: block
;                 padding-right: 20px
;
;             .star
;                 position: absolute
;                 right: 0
;                 top: 0
;                 margin-left: 12px
;                 font-size: 20px
;                 color: #B0BEC5
;                 cursor: pointer
;
;             .star.marked
;                 color: #FFA726
;
;             .price
;                 margin-left: 3px
;                 color: #777
;                 b
;                     color: #076
;                 .fa-rub
;                     font-size: 92%
;
;             .categ
;                 padding: 1px 8px
;
;             .botline
;                 position: absolute
;                 right: 7px
;                 bottom: 2px
;                 font-size: 13px
;                 color: #888
;
;             // .time
;             //     font-size: 92%
;             //     text-align: right
;             //     color: #888
;             //     position: absolute
;             //     right: 7px
;             //     bottom: 3px
;             //     text-align: right
;
;             .moved-up
;                 color: #58b
;                 font-size: 90%
;                 margin-left: 6px
;
;             .tags
;                 margin-top: 2px
;                 margin-left: -1px
;                 margin-right: 50px
;                 font-size: 13px
;                 .tag
;                     m-tag()
;                 .tag:hover
;                     color: #68f
;                 .tag.clicked
;                     background-color: #CE93D8
;
;         .exp-note
;             color: #777
;         .exp-warn
;             color: #c00
;
;         .more-wrap
;             text-align: center
;         .more
;             margin: 8px
;             color: c_a
;             // text-decoration: none
;             // font-weight: bold
;             // border-bottom: 1px dashed c_a
;         .more-arrow-down
;             vertical-align: -1px
;             margin-left: 2px
;
;         .more.msg, more.msg:hover
;             // border-bottom: none
;             color: #aaa
;             font-weight: normal
;
;         @media (min-width: 768px)
;             .item-text
;                 margin-left: -22px
;         @media (max-width: 767px)
;             .sq-wrap
;                 margin-bottom: 5px
;
;
; .bb-my
;     .item:hover
;         background-color: #FFF9C4
;
;
; .tag-search
;     .suggest
;         margin-top: 6px
;         .tag
;             m-tag()
;
;
; .bb-page
;     margin-bottom: 10px
;     position: relative
;
;     .categ
;         margin: 10px 8px 6px 8px
;         text-align: center
;         font-size: 18px
;
;         .fa
;             font-size: 14px
;             margin: 8px
;             vertical-align: 1px
;         .fa.grey
;             color: #aaa
;
;     .title
;         color: c_bbtitle
;         margin: 6px 40px 4px 22px
;         font-size: 28px
;         position: relative
;
;     .star
;         position: absolute
;         right: -22px
;         top: 6px
;         margin-left: 4px
;         font-size: 20px
;         color: #B0BEC5
;         cursor: pointer
;
;     .star.marked
;         color: #FFA726
;
;     .price
;         margin: 0 24px
;         color: #777
;         b
;             color: #076
;
;     .time
;         margin: 6px 10px
;         text-align: right
;         //color: #3949ab
;         // color: #888
;
;     .text
;         margin: 8px 18px 10px 18px
;         padding: 8px 12px
;         background-color: #f6f6f6
;         border-radius: 5px
;
;         em
;             color: c_em
;             font-style: normal
;
;     .tags
;         margin-top: 2px
;         margin-bottom: 10px
;         font-size: 13px
;         .tag
;             m-tag()
;         .tag:hover
;             color: #00f
;
;     .contact
;         margin: 6px 20px 8px 20px
;         .upic
;             width: 32px
;             height: 32px
;         .phone
;             margin-left: 6px
;             margin-right: 8px
;             cursor: pointer
;             .glyphicon
;                 color: c_a
;                 margin: 0 10px 0 4px
;                 font-size: 18px
;                 vertical-align: -2px
;
;     .yashare-auto-init
;         margin-right: 14px
;         margin-top: 1px
;
;     .pics
;         margin: 2px 2px
;         .sq
;             width: 100%
;             height: auto
;             margin: 8px 0
;             border-radius: 4px
;             transition: color ease-in-out .3s
;         .sq:hover
;             box-shadow: 0 0 6px rgba(41,182,246 ,1)
;
;
;     .stat
;         margin: 6px 10px
;
; .bb-tags
;     .b-taglist
;         .item
;             margin: 4px 8px
;             border-bottom: 1px dotted #ddd
;             padding: 0 8px 4px 8px
;             .name
;                 font-size: 16px
;         .related
;             padding-left: 4px
;             .tag:hover
;                 color: #FFF59D
;
; .dlg-bbadd
;     .modal-title
;         color: c_dlg_title
;     .radio-inline
;         margin-right: 10px
;         color: #0277BD
;
; .bb-my
;     .cnt
;         text-align: right
;         white-space: nowrap
;         font-size: 90%
;         margin-top: 1px
;
; .b-bbedit
;     margin-bottom: 10px
;     margin-top: 10px
;
;     .bbid
;         color: c_bbtitle
;         font-size: 18   px
;
;     .period
;         margin: 4px 4px
;         white-space: nowrap
;         display: inline-block
;         padding: 2px 9px 3px 9px
;         background-color: #f5f5f5
;         border-radius: 5px
;         color: #888
;         .dmy
;             color: #000
;
;     .btn-xt
;         text-decoration: none
;         border-bottom: 1px dashed c_a;
;
;     .suggest
;         .tag
;             m-tag()
;
;     .msg
;         margin: 2px 8px
;
;     .save-msg
;         margin: 0 8px 4px 8px
;
;     .b-pics
;         margin-bottom: 8px
;         .sm
;             height: 100px
;             margin: 0 8px 8px 0
;             cursor: pointer
;             padding: 1px
;             border: 1px solid #ddd
;             border-radius: 4px
;
;     .uploadpic_msg
;         white-space: nowrap
;         margin-left: 8px
;
;     .bb-del
;         margin: 0 10px
;         font-weight: bold
;         text-decoration: none
;         border-bottom: 1px dashed #a00
;         color: #a00
;
; .b-categ-menu
;     margin: 10px 0
;     border-radius: 5px
;     background-color: #f5f5f5
;     padding: 3px 5px
;
;     .categ1, .categ2
;         display: inline-block
;         padding: 2px 6px
;         border-radius: 3px
;         overflow: hidden
;
;     .categ1:hover, .categ2:hover
;         background-color: #FFF59D
;
;     .categ1
;         font-size: 17px
;         line-height: 17px
;     .categ2
;         font-size: 15px
;         line-height: 17px
;
;     .sub
;         background-color: #fff
;         margin: 3px 0
;         border-radius: 3px
;         padding: 1px 3px
;
;         .categ1
;             color: #1D47aa
;
;     .curr
;         background-color: #f0f0f4
;
;     @media (min-width: 768px)
;         .categ1, .categ2
;             display: block
;             margin: 3px 0
;         .sub .categ1
;             margin-bottom: 0px
;         .categ2
;             margin: 2px 6px 2px 8px
;             padding: 2px 8px
;
; .bb-add
;     margin: 10px 0 20px 0
;     h2
;         color: c_bbtitle
;         margin-bottom: 10px
;
;
; //.
