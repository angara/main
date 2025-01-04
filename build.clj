(ns build
 (:import
   [java.time LocalDateTime]
   [java.time.format DateTimeFormatter])  
  (:require 
    [clojure.string :refer [trim-newline]]
    [clojure.tools.build.api :as b]
  ,))


(def APPLICATION 'lsn/mgate)
(def VER_MAJOR 0)
(def VER_MINOR 33)
(def MAIN_CLASS 'lsn.mgate.main)


(def CLJ_SRC   "src")
(def JAVA_SRC   "java")

(def TARGET     "target")
(def CLASSES    "target/classes")
(def RESOURCES  "resources")
(def VERSION_FILE "VERSION")


(defn iso-now ^String []
  (.format (LocalDateTime/now) DateTimeFormatter/ISO_LOCAL_DATE_TIME))


(defn clean [_]
  (b/delete {:path TARGET}))


(defn version [_] 
  (format "%s.%s.%s" VER_MAJOR VER_MINOR (b/git-count-revs nil)))


(defn write-version [_]
  (let [ver (str (version nil) "\n")]
    (println "version:" ver)
    (spit VERSION_FILE ver)))


;; https://clojure.org/guides/tools_build

(defn javac [{basis :basis}]
  (b/javac {:src-dirs [JAVA_SRC]
            :class-dir CLASSES
            :basis (or basis (b/create-basis {:project "deps.edn"}))
            :javac-opts ["-Xlint:-options"]
            ; :javac-opts ["-source" "8" "-target" "8"]
           }))


(defn uberjar [_]
  (let [appname   (name APPLICATION)
        version   (trim-newline (slurp VERSION_FILE))
        branch    (b/git-process {:git-args "branch --show-current"})
        commit    (b/git-process {:git-args "rev-parse --short HEAD"})
        timestamp (iso-now)
        uber-file (format "%s/%s.jar" TARGET (name APPLICATION))
        basis     (b/create-basis {:project "deps.edn"})]

    (println "building:" appname version branch commit)

    ;; (javac {:basis basis})

    (b/copy-dir {:src-dirs [CLJ_SRC RESOURCES]
                 :target-dir CLASSES})
    
    (b/compile-clj {:basis basis
                    :src-dirs [CLJ_SRC]
                    :class-dir CLASSES
                    :java-opts [(str "-Dbuild_info.appname="   appname)
                                (str "-Dbuild_info.version="   version)
                                (str "-Dbuild_info.branch="    branch)
                                (str "-Dbuild_info.commit="    commit)
                                (str "-Dbuild_info.timestamp=" timestamp)]
                    ,})

    (b/uber {:basis basis
             :class-dir CLASSES
             :uber-file uber-file
             :main MAIN_CLASS})
    ,))
