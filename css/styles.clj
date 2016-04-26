
(ns usr.styles
  (:require
    [garden.def :refer [defstyles]]
    [garden.units :refer [px]]))
;

(def fa "#fafafa")
(def f8 "#f8f8f8")
(def f4 "#f4f4f4")
(def bd "#bdbdbd")
(def ccc "#ccc")
(def ddd "#ddd")

(def gcurr "#6af")

(def abs-pos
  {:position :absolute :top 0 :left 0 :bottom 0 :right 0})
;

(defstyles main
  [:.header
    {:margin-bottom "8px"
     :padding "4px 12px"
     :border-bottom "1px solid #ddd"
     :background-color f4}
    [:.top-nav]])
;

;;.
