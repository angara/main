(ns angara.webapp.html.core
  (:require
    [hiccup2.core :as hcore]
    [hiccup.util :refer [raw-string]]
  ))


(def raw raw-string)


(defmacro html [opts & content]
  `(hcore/html ~opts ~@content))

(def doctype-html (hcore/raw "<!doctype html>\n"))


(defn html5 [opts & content]
  (html
    doctype-html
    [:html opts content]
   ))


(defn html-resp [body] 
  {:status 200
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body (str body)})


(defn html-status-resp [status body]
  {:status status
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body (str body)})


(comment
  
  (html "123" [:div "456"])

  (html5 [:tag1 {:attr1 "1"}])
  
  ,)
