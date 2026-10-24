(ns angara.webapp.html.util)


(defn make-url [sheme host uri query]
  (str sheme "://" host uri (and query (str "?" query))))
