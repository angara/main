;;
;;  Angara.Net main
;;

(def project {:name "angara.net/main" :version "1.1.0"})

(def jar-main 'web.main)
(def jar-file "main.jar")


(set-env!
  :resource-paths #{"res"}
  :source-paths #{"src"}
  :asset-paths #{"res"}

  ;; boot -d boot-deps ancient
  :dependencies
  '[
    [org.clojure/clojure "1.8.0"]
    [org.clojure/tools.namespace "0.2.11" :scope "test"]
    [org.clojure/core.cache "0.6.5"]

    [org.clojure/tools.logging "0.4.0"]
    [ch.qos.logback/logback-classic "1.2.3"]

    [clj-time "0.13.0"]
    [clj-http "3.6.1"]

    ;; [ring/ring-core "1.5.1"]
    [ring/ring-core "1.6.1"]
    [ring/ring-jetty-adapter "1.6.1"]
    [ring/ring-json "0.4.0"]
    [ring/ring-headers "0.3.0"]

    [cheshire "5.7.1"]
    [compojure "1.6.0"]

    [hiccup "1.0.5"]
    [garden "1.3.2"]
    [mount "0.1.11"]

    [com.novemberain/monger "3.1.0"]

    [org.postgresql/postgresql "42.1.1"]

    ;; https://funcool.github.io/clojure.jdbc/latest/
    [funcool/clojure.jdbc "0.9.0"]
    ;; https://github.com/tomekw/hikari-cp
    [hikari-cp "1.7.6"]

    [honeysql "0.9.0"]  ; https://github.com/jkk/honeysql

    [com.draines/postal "2.0.2"]

    ;; https://github.com/martinklepsch/boot-garden
    [org.martinklepsch/boot-garden "1.3.2-0" :scope "test"]
    [proto-repl "0.3.1" :scope "test"]])
;

(require
  '[clojure.tools.namespace.repl :refer [set-refresh-dirs refresh]]
  '[clojure.edn :as edn]
  '[clj-time.core :as tc]
  '[mount.core :as mount]
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

(defn start []
  (require jar-main)
  (-> "conf/dev.edn"
    (slurp)
    (edn/read-string)
    (mount/start-with-args)))
;

(defn go []
  (mount/stop)
  (apply set-refresh-dirs (get-env :source-paths))
  (refresh :after 'boot.user/start))
;

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

(deftask test-env []
  (set-env! :source-paths #(conj % "test"))
  identity)
;

(deftask dev []
  (set-env! :source-paths #(conj % "test"))
  (apply set-refresh-dirs (get-env :source-paths))
  ;;
  (create-ns 'user)
  (intern 'user 'reset
    (fn []
      (prn "(user/reset)")
      ((resolve 'boot.user/go))))
  ;;
  identity)
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
