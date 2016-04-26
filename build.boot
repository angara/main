;;
;;  Angara.Net main
;;

;; https://github.com/boot-clj/boot/wiki/Boot-Environment

;; boot-reload


; BOOT_EMIT_TARGET=no  ~/.boot/boot.properties

; https://github.com/boot-clj/boot/wiki/Tasks
; https://github.com/boot-clj/boot/wiki/Community-Tasks


(def VER {:name "angara.net/main" :version "0.0.1"})

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
    ; [org.clojure/core.cache "0.6.4"]
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


  repl {:eval (println "Howdy!")
        :init-ns 'user
        :skip-init true}
)


(deftask make-build-edn []
  (with-pre-wrap fs
    (let [t (tmp-dir!)]
      (spit (clojure.java.io/file t "build.edn") VER)
      (prn "build.edn:" VER)
      (-> fs (add-resource t) commit!))))
;

; (deftask dist []
;   (comp
;     (pom :project 'my-project :version "1.2.3")
;     (make-build-edn)
;     (uber)
; ;    (aot :namespace '#{my-project.core})
;     (jar :main 'my-project.core)))


(deftask bld []
  (comp
;    (aot :all true)
    (make-build-edn)
    (uber)
    (jar :main 'web.main :file "main.jar")
    (target :dir #{"tmp/target"})
  ))


;; (require '[clojure.tools.namespace.repl :as repl])
;; (apply repl/set-refresh-dirs (get-env :resource-paths))
;; (repl/refresh)

; (require '[demo.boot-build :refer :all])

; (deftask build
;   "Build my project."
;   []
;   (comp (pom) (jar) (install)))

(deftask null-task
  "Does nothing."
  []
  (fn [next-task]
    (fn [fileset]
      (next-task fileset))))

; (deftask null-task
;   "Does nothing."
;   []
;   clojure.core/identity)

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

;;.
