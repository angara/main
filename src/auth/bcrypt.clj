(ns auth.bcrypt
  (:import
    [org.mindrot.jbcrypt BCrypt])
  (:require
    [taoensso.telemere :refer [log!]]))


; http://www.mindrot.org/projects/jBCrypt/

(defn check-pass [password hash]
  (try
    (BCrypt/checkpw password hash)
    (catch Exception ex
      (log! :warn ["check-pass:" ex]))))


(defn hash-pass [password]
  (try
    (BCrypt/hashpw password (BCrypt/gensalt))
    (catch Exception ex
      (log! :warn ["hash-pass:" ex]))))
