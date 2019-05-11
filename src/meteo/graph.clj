
(ns meteo.graph
  (:require
    [clojure.string :refer [join]]
    [hiccup.core :refer [html]]
    [mlib.logger :refer [debug info warn]]))
;

(def SVG_ATTR 
  {:xmlns "http://www.w3.org/2000/svg" :baseProfile "full" :version "1.1"})
;;  viewBox="0 0 100 100"


(def PAD_Y 10)
(def PAD_X 10)

(def Y_NUM 40)
(def Y_STEP 2)

(def X_NUM 72)
(def X_STEP 5)

(def GRID_W (* X_NUM X_STEP))
(def GRID_H (* Y_NUM Y_STEP))

(def T3_WIDTH (+ PAD_X PAD_X (* X_NUM X_STEP)))
(def T3_HEIGHT (+ PAD_Y PAD_Y (* Y_NUM Y_STEP)))


(def y-axis 
  { :padding 10
    :labels ["-20" "-10"]
    :color "red"})
;

(def x-axis
  { :padding 10
    :color "#333333"}) 
  
;

(def labels-y (reverse ["-20" "-10" "0" "10" "20"]))
(def label-y-step (* Y_STEP 10))


(comment)
  
(defn h-path []
  (let [x0 (str PAD_X)
        wd (str GRID_W)]
    (->> 
      (range (count labels-y))
      (reduce 
        (fn [[path y] _]
          [
            (conj path "M" x0 (str y) "H" wd) 
            (+ y label-y-step)])
        [[] PAD_Y])
      first)))
;

(defn grid [x0 y0]
  ; (let [path ["m" (str x0) (str y0)]
  ;       path (apply conj [path "h" (str GRID_W)])]
    [:g {:class "grid"}
      [:path{
              :d (join " " (h-path))
              ; :d "M 10 10 H 360 M 10 30 H 360" ; m 10 50 h 360 m 10 70 h 360 m 10 90 h 360"
              :stroke (:color y-axis)
              :stroke-width "1"}]])
;      

(comment      
  (grid 10 10)            
  .)
      ; line {:x1 PAD_X
      ;         :y1 PAD_Y
      ;         :x2 PAD_X
      ;         :y2 (+ PAD_Y (* Y_NUM Y_STEP))
      ;         :stroke (:color y-axis)
      ;         :stroke-width "2"}]))
;

(defn svg [attr & content]
  [:svg (merge SVG_ATTR attr) content])
;

(def T3
  (svg {:width T3_WIDTH :height T3_HEIGHT}
    [:rect {:width "100%" :height "100%" :fill "#f4f4f4"}]
    (grid PAD_X PAD_Y)
 
    [:circle {:cx "150" :cy "100" :r "80" :fill "green"}]  
    [:text {:x "150" :y "125" :font-size "60" :text-anchor "middle" :fill "white"} "SVG"]))
;

(defn t3-svg [{params :params}]
  (prn "params:" params)
  { :status 200
    :headers {"Content-Type" "image/svg+xml"}
    :body (html T3)})
;

;;.
