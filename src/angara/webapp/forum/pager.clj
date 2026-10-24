(ns angara.webapp.forum.pager
  (:require 
   [angara.webapp.html.core :refer [html]]
   ))


(defn bound-int [i lower upper]
  (if i
    (min upper (max lower i))
    lower))


(defn num-pages [num-items page-size] 
  (quot (+ num-items page-size -1) page-size))


(def PAGER_SOLID 12)
(def PAGER_BORDER 3)
(def PAGER_CENTER 1)  ;; half of central pad


(defn- pager-item [p curr link-fn]
  ;; disabled tabindex=-1
  (let [item-cls (if (= p curr) "pager-item-active" "pager-item")]
    [:li [:a {:class item-cls :href (link-fn p)} (str p)]]
    ))


(defn pager-block [link-fn curr num]
  (when (and num (< 1 num))
    (html
     [:div.mx-auto "\n"
      [:ul.flex.flex-wrap.justify-center.list-style-none.mx-8
       (if (<= num PAGER_SOLID)
         (for [p (range 1 (inc num))]
           (pager-item p curr link-fn))
         (list
          (for [p (range 1 (inc PAGER_BORDER))]
            (pager-item p curr link-fn))
          (when (> curr (+ PAGER_BORDER PAGER_CENTER 1))
            [:li [:span.pager-item-disabled "···"]])
          (for [p (range (max (- curr PAGER_CENTER) (inc PAGER_BORDER))
                         (min (+ curr PAGER_CENTER 1)
                              (- num PAGER_BORDER -1)))]
            (pager-item p curr link-fn))
          (when (< curr (- num PAGER_BORDER PAGER_CENTER))
            [:li [:span.pager-item-disabled "···"]])  ;; unicode middle dot U+00B7
          (for [p (range (- num PAGER_BORDER -1) (inc num))]
            (pager-item p curr link-fn))))]
      "\n"]
     )))
