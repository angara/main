(ns meteo.graph
  (:require
    [clojure.string :refer [join]]
    [hiccup.core :refer [html]]
   ,))


(def SVG_ATTR 
  {:xmlns "http://www.w3.org/2000/svg" :baseProfile "full" :version "1.1"})
;;  viewBox="0 0 100 100"

(def PAD_Y 10)
(def PAD_X 10)

(def Y_NUM 40)
(def Y_STEP 10)
(def Y_LABEL_STEP 100)
(def Y_TICK 4)
(def Y_LABEL_HEIGHT 20)

(def X_NUM 72)
(def X_STEP 10)
(def X_TICK 5)

(def Y_LABEL_WIDTH 100)

(def GRID_X0 (+ PAD_X Y_LABEL_WIDTH))
(def GRID_Y0 PAD_Y)

(def GRID_W (* X_NUM X_STEP))
(def GRID_H (* Y_NUM Y_STEP))

(def T3_WIDTH 
  (+ 
    PAD_X 
    Y_LABEL_WIDTH
    GRID_W
    PAD_X))


(def T3_HEIGHT 
  (+ 
    PAD_Y 
    GRID_H
    PAD_Y))


(def y-axis 
  { :padding 10
    :labels ["-20" "-10"]
    :color "#222222"})


(def x-axis
  { :padding 10
    :color "#222233"}) 


(def labels-y (reverse ["-20" "-10" "0" "10" "20"]))
(def label-y-step (* Y_STEP 10))


(def labels-x ["0" "1" "2" "3"])
(def label-x-step (* X_STEP 24))
  

(defn h-path [x0 y0 len num step]
  (let [len (str (+ x0 len))
        x0  (str x0)]
    (->> 
      (range num)
      (reduce 
        #(conj %1 "M" x0 (str (+ y0 (* %2 step))) "H" len) 
        [])
      (join " "))))


(comment
  (h-path 10 8 100 2 5)
  ,)


(defn v-path [x0 y0 len num step]
  (let [len (str (+ y0 len))
        y0 (str y0)]
    (->> 
      (range num)
      (reduce 
        #(conj %1 "M" (str (+ x0 (* %2 step))) y0 "V" len) 
        [])
      (join " "))))


(defn grid [x0 y0]
  (let [num-y (count labels-y)
        num-x (count labels-x)
        y0_thin (+ y0 (/ label-y-step 2))
        step label-y-step]
    [:g {:class "grid"}
      ;; 10 degrees
      [:path
        {
          :d (h-path x0 y0 GRID_W num-y step)
          :stroke (:color y-axis)
          :stroke-width "0.6"}]
      ;; secondary
      [:path
        {
          :d (h-path x0 y0_thin GRID_W (dec num-y) step)
          :stroke (:color y-axis)
          :stroke-dasharray "3,3"
          :stroke-width "0.3"}]
      ;; days
      [:path
        {
          :d (v-path x0 y0 GRID_H num-x label-x-step)
          :stroke (:color x-axis)
          :stroke-width "0.6"}]]))
      

(defn draw-labels-y [x0 y0 labels]
  (let [x0 (- x0 Y_LABEL_WIDTH)
        y0 (+ y0 (/ Y_LABEL_HEIGHT 2))
        dy Y_LABEL_STEP]
    [:g {:class "labels-y"}
      (->> labels
        (map-indexed
          (fn [i t]
            [:text {:x x0 :y (+ y0 (* i dy)) :text-anchor "middle"} t])))]))

;; text-anchor="middle"

(defn svg [attr & content]
  [:svg (merge SVG_ATTR attr) content])


(def T3
  (svg {:width T3_WIDTH :height T3_HEIGHT}
    [:style ".labels-y {font-size: " Y_LABEL_HEIGHT " px;}"]
    [:rect {:width "100%" :height "100%" :fill "#f4f4f4"}]
    (grid GRID_X0 GRID_Y0)
    (draw-labels-y GRID_X0 GRID_Y0 labels-y)))
 
    ; [:circle {:cx "150" :cy "100" :r "80" :fill "green"}]  
    ; [:text {:x "150" :y "125" :font-size "60" :text-anchor "middle" :fill "white"} "SVG"]))


(defn t3-svg [{_params :params}]
  ; (prn "params:" params)
  { :status 200
    :headers {"Content-Type" "image/svg+xml"}
    :body (html T3)})
