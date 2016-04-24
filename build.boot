(set-env!
  :resource-paths #{"src"}
  :dependencies '[[me.raynes/conch "0.8.0"]]
  :target-path "out"
  )

(task-options!
  pom {:project 'my-project
       :version "0.1.0"}
  jar {:manifest {"Foo" "bar"}})

; (require '[demo.boot-build :refer :all])

(deftask build
  "Build my project."
  []
  (comp (pom) (jar) (install)))

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
