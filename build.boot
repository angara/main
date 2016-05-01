;;
;;  Angara.Net main
;;

;; https://github.com/boot-clj/boot/wiki/Boot-Environment

; BOOT_EMIT_TARGET=no  ~/.boot/boot.properties

; https://github.com/boot-clj/boot/wiki/Tasks
; https://github.com/boot-clj/boot/wiki/Community-Tasks


(def VER {:name "angara.net/main" :version "0.1.0"})

; (set-env! :source-paths #{"foo" "bar"})
; (set-env! :source-paths #(conj % "baz"))
(set-env!
  :resource-paths #{"src" "res"}
  ; :source-paths #{...}
  :asset-paths #{"res"}

  ;; boot -d boot-deps ancient
  :dependencies
  '[
    [org.clojure/clojure "1.8.0"]
    [org.clojure/tools.namespace "0.2.11" :scope "test"]
    ; [org.clojure/tools.logging "0.3.1"]
    [com.taoensso/timbre "4.3.1"]   ; https://github.com/ptaoussanis/timbre
    [org.clojure/core.cache "0.6.4"]
    ; [ch.qos.logback/logback-classic "1.1.6"]

    [clj-time "0.11.0"]
    [clj-http "3.0.1"]

    [javax.servlet/servlet-api "2.5"]
    [http-kit "2.1.19"]
    [ring/ring-core "1.4.0"]
    [ring/ring-json "0.4.0"]
    [ring/ring-headers "0.2.0"]

    [cheshire "5.6.1"]
    [compojure "1.5.0"]
    [hiccup "1.0.5"]
    [mount "0.1.10"]

    [com.novemberain/monger "3.0.2"]
    [org.clojure/java.jdbc "0.5.8"]
    [org.postgresql/postgresql "9.4.1208"]
    [com.mchange/c3p0 "0.9.5.2"]
    [honeysql "0.6.3"]  ; https://github.com/jkk/honeysql

    ;; [com.draines/postal "1.11.3"]
    ;; [enlive "1.1.5"]     ;; https://github.com/cgrand/enlive
  ]

;  :target-path "tmp/target"
)


;   [adzerk/boot-test "1.1.0" :scope "test"]])


(task-options!
;   pom {:project 'my-project
;        :version "0.1.0"}
;   jar {:manifest {"Foo" "bar"}}

  aot {}

  repl {; :init-ns 'web
        ; :skip-init true
       }
)

(require
  '[clojure.tools.namespace.repl :as repl]
  '[clj-time.core :as tc]
  '[boot.git :refer [last-commit]]
  '[mount.core :as mount]
  '[mlib.core :refer [edn-read]]
  '[mlib.conf :refer [run-conf]]
  '[web.app :as app]
)


(defn increment-build []
  (let [bf "res/build.edn"
        num (:num (edn-read bf))
        out (merge VER {
                :timestamp (str (tc/now))
                :commit (last-commit)
                :num (inc num)
              })
        ]
    (spit bf (.toString out))
  ))

(defn start []
  (reset! run-conf (edn-read "var/dev.edn"))
  (mount/start))
;

(defn go []
  (mount/stop)
  (apply repl/set-refresh-dirs (get-env :resource-paths))
  (repl/refresh :after 'boot.user/start))
;

(deftask dev []

  )
;


(deftask dist []
  (increment-build)
  (comp
    ;(pom :project 'my-project :version "1.2.3")
    (aot)
    (uber)
    (jar :main 'web.main :file "main.jar")
    (target :dir #{"tmp/target"})
  ))


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
