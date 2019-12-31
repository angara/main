(ns mlib.random
  (:import
    [java.security SecureRandom]))
;=

(def random (SecureRandom.))

(defn random-bytes ^bytes [^long n]
  (let [b (byte-array n)]
    (.nextBytes random b)
    b))
;;

(defn urand32 []
  (reduce
    #(+' (*' 256 %1) (bit-and 255 %2))
    0
    (random-bytes 4)))
;;

(defn urand64 ^BigInteger []
  (BigInteger. 1 (random-bytes 8)))
;;

;;.
