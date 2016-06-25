(ns front.core
  (:require
    [taoensso.timbre :refer [warn]]
    [mlib.conf :refer [conf]]
    [html.frame :refer [layout]]))
;

(defn main-page [req]
  (layout req {}
    "Angara.Net main page"))


;;.
