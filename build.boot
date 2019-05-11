;;
;;  Angara.Net main
;;

(def project {:name "angara.net/main" :version "1.4.0"})

(def jar-main 'web.main)
(def jar-file "main.jar")


(set-env!
  :resource-paths #{"resources"}
  :source-paths #{"src"}
  :asset-paths #{"resources"}

  ;; boot -d boot-deps ancient
  :dependencies
  '[
    [org.clojure/clojure "1.10.0"]
 ;   [org.clojure/tools.namespace "0.2.11" :scope "test"]
    [org.clojure/core.cache "0.7.2"]

    [org.clojure/tools.logging "0.4.1"]
    [ch.qos.logback/logback-classic "1.2.3"]

    [clj-time "0.15.1"]
    [clj-http "3.10.0"]

    [ring/ring-core "1.7.1"]
    [ring/ring-jetty-adapter "1.7.1"]
    [ring/ring-json "0.4.0"]
    [ring/ring-headers "0.3.0"]

    [cheshire "5.8.1"]
    [compojure "1.6.1"]

    [hiccup "1.0.5"]
    [garden "1.3.9"]
    [mount "0.1.16"]

    [com.novemberain/monger "3.5.0"]

    [org.postgresql/postgresql "42.2.5"]

    ;; https://funcool.github.io/clojure.jdbc/latest/
    [funcool/clojure.jdbc "0.9.0"]
    
    ;; https://github.com/tomekw/hikari-cp
    [hikari-cp "2.7.1"]
    
    ;; https://github.com/jkk/honeysql
    [honeysql "0.9.4"]  

    [com.draines/postal "2.0.3"]

    ;; https://github.com/martinklepsch/boot-garden
    [org.martinklepsch/boot-garden "1.3.2-1" :scope "test"]])
;

(require
  ; '[clojure.tools.namespace.repl :refer [set-refresh-dirs refresh]]
  '[clojure.edn :as edn]
  '[clj-time.core :as tc]
  ; '[mount.core :as mount]
  '[boot.git :refer [last-commit]]
  '[org.martinklepsch.boot-garden :refer [garden]])
;

(task-options!
  garden
  {
    :styles-var 'css.root/main
    :output-to  "public/incs/css/main.css"
    :pretty-print false})
;

;;; ;;; ;;; ;;;

; (defn start []
;   (require jar-main)
;   (-> "conf/dev.edn"
;     (slurp)
;     (edn/read-string)
;     (mount/start-with-args)))
; ;

; (defn go []
;   (mount/stop)
;   (apply set-refresh-dirs (get-env :source-paths))
;   (refresh :after 'boot.user/start))
; ;

;;; ;;; ;;; ;;;

(defn increment-build []
  (let [bf "res/build.edn"
        num (-> bf slurp edn/read-string :num)
        bld { :timestamp (str (tc/now))
              :commit (last-commit)
              :num (inc num)}]
    (spit bf (.toString (merge project bld)))))
;

(deftask css-dev []
  (comp
    (garden :pretty-print true)
    (target :dir #{"tmp/res/"})))
;

(deftask dev []
  (set-env! :source-paths #(conj % "tools"))
  ; (apply set-refresh-dirs (get-env :source-paths))
  ;;
  ; (create-ns 'user)
  ; (intern 'user 'reset
  ;   (fn []
  ;     (prn "(user/reset)")
  ;     ((resolve 'boot.user/go))))
  ; ;;
  ;; cider)
  repl)
;

(deftask build []
  (increment-build)
  (comp
    ;; (javac)
    (garden)
    (aot :all true)
    (uber)
    (jar :main jar-main :file jar-file)
    (target :dir #{"tmp/target"})))
;

;;.
