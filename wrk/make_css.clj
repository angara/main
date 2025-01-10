
(ns make-css
  (:require
    [garden.core :refer [css]]
    [css.root])) 


(defn write-out [style fname pretty?]
  (css {:output-to fname :pretty-print? pretty?} style))


(defn -main [& args]
  (if-let [css-file (first args)]
    (write-out css.root/main css-file (second args))
    (println "No CSS file specified!")))
