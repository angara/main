;;
;;  Angara.Net main
;;

;; https://github.com/boot-clj/boot/wiki/Boot-Environment

; https://github.com/boot-clj/boot/wiki/Tasks
; https://github.com/boot-clj/boot/wiki/Community-Tasks


(def VER {:name "angara.net/main" :version "0.7.2"})

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

    [clj-time "0.12.0"]
    [clj-http "3.2.0"]

    [ring/ring-core "1.5.0"]
    [ring/ring-json "0.4.0"]
    [ring/ring-headers "0.2.0"]
    [ring/ring-jetty-adapter "1.5.0"]

    [cheshire "5.6.3"]
    [compojure "1.5.1"]
    ;; [hiccup "1.0.5"]
    [rum "0.10.6"]
    [garden "1.3.2"]
    [mount "0.1.10"]

    [com.novemberain/monger "3.0.2"]
    [org.clojure/java.jdbc "0.6.1"]
    [org.postgresql/postgresql "9.4.1209"]
    [com.mchange/c3p0 "0.9.5.2"]
    [honeysql "0.8.0"]  ; https://github.com/jkk/honeysql

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
  '[mount.core]
  '[web.srv])
;

(task-options!
  aot {:all true}
  garden {
          :styles-var 'css.styles/main
          :output-to  "public/incs/css/main.css"
          :pretty-print false})
;

(defn start []
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
        out (merge VER
              { :timestamp (str (tc/now))
                :commit (last-commit)
                :num (inc num)})]
    (spit bf (.toString out))))
;

(deftask css-dev []
  (comp
    (watch)
    (garden :pretty-print true)
    (target :dir #{"tmp/res/"})))
;

(deftask dev []
  (repl))
;

(deftask build []
  (increment-build)
  (comp
    (garden)
    (aot)
    (uber)
    (jar :main 'web.main :file "main.jar")
    (target :dir #{"tmp/target"})))
;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


; (deftask run []
;   (with-pre-wrap fileset
;     (my.namespace/-main)
;     fileset))

;; uberjar:
;
; (set-env!
;  :resource-paths #{"src"}
;  :dependencies '[[org.clojure/clojure "1.6.0"     :scope "provided"]])
;
; (deftask build
;   "Builds an uberjar of this project that can be run with java -jar"
;   []
;   (comp
;    (aot :namespace '#{my-namespace})
;    (pom :project 'myproject
;         :version "1.0.0")
;    (uber)
;    (jar :main 'my_namespace)))
;
; (defn -main [& args]
;   (require 'my-namespace)
;   (apply (resolve 'my-namespace/-main) args))


;; https://github.com/danielsz/boot-shell

; (set-env! :dependencies ‘[[danielsz/boot-shell "X.X.X"]])
; (require '[danielsz.boot-runit :refer [shell]])

; (deftask deploy
;   []
;   (comp
;    (cljs :optimizations :advanced)
;    (build)
;    (runit :env {:http-port 8010
;                 :db-url "link/to/db"
;                 :repl-port 8013}
;           :restart true)
;    (shell :password "base64-encoded-string" :script "commit.sh")))


;; https://github.com/adzerk-oss/boot-cljs

; (set-env! :dependencies '[[adzerk/boot-cljs "1.7.228-1" :scope "test"]])
; (require '[adzerk.boot-cljs :refer [cljs]])


;; https://github.com/adzerk-oss/boot-reload

; (set-env! :dependencies '[[adzerk/boot-reload "0.4.5" :scope "test"]])
; (require '[adzerk.boot-reload :refer [reload]])


;; https://github.com/JulianBirch/cljs-ajax

;; https://github.com/martinklepsch/boot-garden


;
; (deftask build-cljs []
;   (set-env!
;    :source-paths #(conj % "src-cljs-prod"))
;   (cljs :optimizations :advanced))
;
; (deftask build []
;   (comp
;    (build-cljs)
;    (aot :namespace '#{animals.uberjar})
;    (pom :project 'animals
;         :version "1.0.0")
;    (uber)
;    (jar :main 'animals.uberjar)))


; (pod/with-pod @pod
;         (require '[boot.pod :as pod])
;         (require '[boot.util :as util])
;         (require '[boot.repl :as repl])
;         (require '[clojure.tools.namespace.repl :as tnsr])
;
;         (util/info "Launching %s...\n" ~pod-env)
;         (util/info "Launching backend nRepl...\n")
;
;
;         (apply tnsr/set-refresh-dirs (-> pod/env :directories))
;
;         (repl/launch-nrepl {:init-ns '~init-ns
;                             :port ~port
;                             :server true
;                             :middleware (:middleware pod/env)})
;
;         (require 'dev)
;         (require 'reloaded.repl)
;         (reloaded.repl/go))


; (deftask dev
;          "Start the dev env..."
;          [s speak bool "Notify when build is done"
;           p port PORT int "Port for web server"]
;          (merge-env! :resource-paths #{"env/dev/js"})
;          (comp
;            (watch)
;            (reload :on-jsload 'torcaui.core/reinstall-om!)
;            (cljs-repl)
;            (cljs :ids #{"main" "devcards"})
;            (serve :port port :dir "target" :not-found 'reps.server/not-found-handler  :reload true)
;            (target)
;            (if speak (boot.task.built-in/speak) identity)))
;
; (deftask package
;          "Build the package"
;          []
;          (merge-env! :resource-paths #{"env/prod/js"})
;          (comp
;            (cljs :compiler-options {
;                                     :devcards false
;                                     :optimizations :advanced
;                                     :externs ["externs/jquery-1.9.js"]})
;
;            (target)))


;; https://github.com/pandeiro/boot-http

;;.
