(ns angara.webapp.html.icons
  (:require
   [hiccup2.core :refer [raw]]
  ,))


;; https://icons.getbootstrap.com/
;; https://www.iconfinder.com/search?price=free

(def svg-link-12
  (raw
   (str "<svg width=\"12\" height=\"12\" fill=\"none\" stroke=\"currentColor\""
        " stroke-linecap=\"round\" stroke-linejoin=\"round\" stroke-width=\"2\" viewBox=\"0 0 24 24\">"
        "<path d=\"M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71\"/>"
        "<path d=\"M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71\"/></svg>")))

;; https://github.com/SamHerbert/SVG-Loaders
