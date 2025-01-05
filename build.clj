(ns build
 (:import
   [java.time LocalDateTime]
   [java.time.format DateTimeFormatter])  
  (:require 
    [clojure.java.io :as io]
    [clojure.tools.build.api :as b]
  ,))


(def APP_NAME   "angara-main")
(def VER_MAJOR  2)
(def VER_MINOR  0)
(def MAIN_CLASS 'app.main)

(def JAR_NAME   "angara-main.jar")

(def CLJ_SRC    "src")
(def JAVA_SRC   "java")
(def TARGET     "target")
(def CLASSES    "target/classes")
(def RESOURCES  "resources")
(def BUILD_INFO "build-info.edn")


(defn iso-now ^String []
  (.format (LocalDateTime/now) DateTimeFormatter/ISO_LOCAL_DATE_TIME))


(defn clean [_]
  (b/delete {:path TARGET}))


(defn version [_] 
  (format "%s.%s.%s" VER_MAJOR VER_MINOR (b/git-count-revs nil)))


(defn build-info [_]
  {:appname APP_NAME
   :version (version nil)
   :branch (b/git-process {:git-args "branch --show-current"})
   :commit (b/git-process {:git-args "rev-parse --short HEAD"})
   :timestamp (iso-now)}
  ,)


(defn write-build-info [build-info]
  (let [out-file (io/file CLASSES BUILD_INFO)]
    (io/make-parents out-file)
    (spit out-file (pr-str build-info))))


;; https://clojure.org/guides/tools_build

(defn javac [{basis :basis}]
  (b/javac {:src-dirs [JAVA_SRC]
            :class-dir CLASSES
            :basis (or basis (b/create-basis {:project "deps.edn"}))
            :javac-opts ["-Xlint:-options"]
            ; :javac-opts ["-source" "8" "-target" "8"]
           }))


(defn uberjar [_]
  (let [build-info (build-info nil)
        uber-file (io/file TARGET JAR_NAME)
        basis (b/create-basis {:project "deps.edn"})]

    (println "building:" build-info) 

    (write-build-info build-info)

    ;; (javac {:basis basis})

    (b/copy-dir {:src-dirs [CLJ_SRC RESOURCES]
                 :target-dir CLASSES})
    
    (b/compile-clj {:basis basis
                    :src-dirs [CLJ_SRC]
                    :class-dir CLASSES})

    (b/uber {:basis basis
             :class-dir CLASSES
             :uber-file (str uber-file)
             :main MAIN_CLASS})
    
    (println "complete:" (str uber-file))
    ,))
