;;
;;  Angara.Net main
;;

(def project
  {:name "angara.net/main"
   :version "0.8.0"})

(def jar-main 'web.main)
(def jar-file "main.jar")
(def dev-main 'web.srv)

(set-env!
  :resource-paths #{"res"}
  :source-paths #{"src"}
  :asset-paths #{"res"}

  ;; boot -d boot-deps ancient
  :dependencies
  '[
    [org.clojure/clojure "1.8.0"]
    [org.clojure/tools.namespace "0.2.11" :scope "test"]
    [com.taoensso/timbre "4.7.4"]   ; https://github.com/ptaoussanis/timbre
    [org.clojure/core.cache "0.6.5"]

    [clj-time "0.12.2"]
    [clj-http "3.3.0"]

    [ring/ring-core "1.5.0"]
    [ring/ring-json "0.4.0"]
    [ring/ring-headers "0.2.0"]
    [ring/ring-jetty-adapter "1.5.0"]

    [cheshire "5.6.3"]
    [compojure "1.5.1"]

    [rum "0.10.7"]
    [garden "1.3.2"]
    [mount "0.1.10"]

    [com.novemberain/monger "3.1.0"]

    [org.postgresql/postgresql "9.4.1212"]

    ;; https://funcool.github.io/clojure.jdbc/latest/
    [funcool/clojure.jdbc "0.9.0"]
    ;; https://github.com/tomekw/hikari-cp
    [hikari-cp "1.7.5"]

    [honeysql "0.8.1"]  ; https://github.com/jkk/honeysql

    ;; https://github.com/martinklepsch/boot-garden
    [org.martinklepsch/boot-garden "1.3.2-0" :scope "test"]])
    ;; [com.draines/postal "1.11.3"]
    ;; [enlive "1.1.5"]     ;; https://github.com/cgrand/enlive

;

(require
  '[clojure.tools.namespace.repl :refer [refresh set-refresh-dirs]]
  '[clojure.edn :as edn]
  '[clj-time.core :as tc]
  '[boot.git :refer [last-commit]]
  '[org.martinklepsch.boot-garden :refer [garden]]
  '[mount.core])
;

(task-options!
  aot {:all true}
  garden {
          :styles-var 'css.styles/main
          :output-to  "public/incs/css/main.css"
          :pretty-print false})
;

(defn start []
  (require dev-main)
  (mount.core/start-with-args
    (-> "var/dev.edn" slurp edn/read-string)))
;

(defn go []
  (mount.core/stop)
  (apply set-refresh-dirs (get-env :source-paths))
  (refresh :after 'boot.user/start))
;

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
    (watch)
    (garden :pretty-print true)
    (target :dir #{"tmp/res/"})))
;

(deftask test-env []
  (set-env! :source-paths #(conj % "test"))
  identity)
;

(deftask dev []
  (comp
    (test-env)
    ;; (javac)
    (repl)))
;

(deftask build []
  (increment-build)
  (comp
    ;; (javac)
    (garden)
    (aot)
    (uber)
    (jar :main jar-main :file jar-file)
    (target :dir #{"tmp/target"})))
;

;;.
