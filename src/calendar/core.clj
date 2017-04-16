
(ns calendar.core
  (:require
    [clojure.java.shell :refer [sh]]
    [compojure.core :refer [defroutes GET POST]]  ;context ANY
    [mlib.conf :refer [conf]]
    [mlib.http :refer [json-resp]]
    ;
    [calendar.html :refer [index-page]]
    [forum.db :refer
      [get-topic get-messages attach-params]]))
;


(defn thumb-100 [orig dest]
  (when (and orig dest)
    (sh "convert" orig ;; "-auto-gamma" "-auto-level"
        "-auto-orient"
        "-filter" "Lanczos"
        "-unsharp" "0x1"
        "-thumbnail" "100x100^"
        "-gravity" "center"
        "-extent" "100x100"
        "-strip"
        "-quality" "80" dest)))
;

(defn topic-info [tid]
  (when-let [topic (get-topic tid)]
    (let [msg (first (get-messages tid 0 1))
          att (attach-params msg)]
      {:topic topic :msg msg :attach att})))
;

(defn tinfo-calendar [tinfo]
  (when tinfo
    (let [cnf (:forum conf)
          dir (:upload-dir cnf)
          {type :type base :base} (:attach tinfo)
          t100 (str base "_100.jpg")
          thumb (when (= "jpg" type)
                  (thumb-100
                    (str dir base ".jpg")
                    (str dir t100))
                  (str (:upload-uri cnf) t100))
          topic (:topic tinfo)]
      ;
      {
        :uid    (str (:owner topic))
        :title  (:title topic)
        :descr  (-> tinfo :msg :body)
        :link   (str "/forum/t" (:tid topic))
        :thumb  thumb})))
        ;; tags by tgroup ???
;


(defroutes calendar-routes
  (GET "/" [] index-page))
  ; (ANY      "/*"      _ (json-resp {:err :req})))
;

#_ (topic-info 113992)

;;
