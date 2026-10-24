;;
;;  mlib: 0.8.0  (merged from angara/usr)
;;

(ns mlib.core
  (:require
    [clojure.string :as s]
    [clojure.java.io :as io]
    [clojure.edn :as edn]
   ,)
  (:import
    [java.security MessageDigest]))


(defn assoc-not-nil [m k v]
  (if (nil? v) m (assoc m k v)))


;; TODO: replace with (parse-long)
(defn to-int
  "returns nil or default on failure"
  ( ^Integer [s]
    (to-int s nil))
  ( ^Integer [s default]
    (try
      (if (string? s) (Integer/parseInt s) (int s))
      (catch Exception _ignore default))))


(defn to-long
  "returns nil or default on failure"
  ( ^Integer [s]
    (to-long s nil))
  ( ^Integer [s default]
    (try
      (if (string? s) (Long/parseLong s) (long s))
      (catch Exception _ignore default))))


(defn now-ms []
  (System/currentTimeMillis))


(defn to-float
  "returns nil or default on failure"
  ( ^Float [s]
    (to-float s nil))
  ( ^Float [s default]
    (try
      (if (string? s) (Float/parseFloat s) (float s))
      (catch Exception _ignore default))))


(defn to-double
  "returns nil or default on failure"
  ( ^Double [s]
    (to-double s nil))
  ( ^Double [s default]
    (try
      (if (string? s) (Double/parseDouble s) (double s))
      (catch Exception _ignore default))))


(defn urand-bytes [n]
  (with-open [in (io/input-stream (io/file "/dev/urandom"))]
    (let [buf (byte-array n)]
      (.read in buf)
      buf)))


(defn urand32 []
  (reduce #(+ (* 256 %1) (bit-and 255 %2)) 0 (urand-bytes 4)))



;;;;;; edn ;;;;;;

(defn edn-read [file]
  (edn/read-string (slurp file)))


(defn edn-resource [res]
  (-> res io/resource slurp edn/read-string))



;; -- patterns --

(defn email?
  [^String s]
  (and
    (string? s)
    (<= (.length s) 80)
    (re-matches #"(?i)([0-9a-z\_\.\-\+]+)@([0-9a-z\-]+\.)+([a-z]){2,}" s)
    s))


(defn phone?
  "matches only +7 phones!"
  [^String phone]
  (and
    (string? phone)
    (re-matches #"\+7\d{10}" phone)
    phone))



;; -- string utils --

(defn str-trim ^String [s]
  (s/trim (str s)))


(defn str-head
  "Returns the first n characters of s."
  ^String [n ^String s]
  (if (>= n (.length s)) s (.substring s 0 n)))


(defn str-tail
  "Returns the last n characters of s."
  ^String [n ^String s]
  (if (< (count s) n) s (.substring s (- (count s) n))))


(defn hesc
  "Replace special characters by HTML character entities."
  [text]
  (s/escape (str text)
    {\& "&amp;" \< "&lt;" \> "&gt;" \" "&quot;" \' "&apos;"}))


(defn cap-first [s]
  (when (string? s)
    (if (> (.length s) 0)
      (str (Character/toUpperCase (.charAt s 0)) (.substring s 1))
      s)))



;;;;;;  hashes  ;;;;;;

(defn hexbyte ^String [^Integer b]
    (.substring (Integer/toString (+ 0x100 (bit-and 0xff b)) 16) 1))


(defn byte-array-hash
  "calculate hash of byte array"
  [hash-name barray]
  (let [md (MessageDigest/getInstance hash-name)]
    (.update md barray)
    (.digest md)))


(defn calc-hash
  "calculate hash byte array of utf string using hash-function"
  [hash-name s]
  (let [md (MessageDigest/getInstance hash-name)]
    (.update md (.getBytes s "UTF-8"))
    (.digest md)))


(defn md5
  "returns md5 lowercase string calculated on utf-8 bytes of input"
  ^String [^String s]
  (apply str (map hexbyte (calc-hash "MD5" s))))


(defn sha1
  "returns sha1 lowercase string calculated on utf-8 bytes of input"
  ^String [^String s]
  (apply str (map hexbyte (calc-hash "SHA-1" s))))


(defn sha256
  "returns sha256 lowercase string calculated on utf-8 bytes of input"
  ^String [^String s]
  (apply str (map hexbyte (calc-hash "SHA-256" s))))



;;;;;; deep merge ;;;;;;

(defn- deep-merge* [& maps]
  (let [f (fn [old new]
            (if (and (map? old) (map? new))
                (merge-with deep-merge* old new)
                new))]
    (if (every? map? maps)
      (apply merge-with f maps)
      (last maps))))


(defn deep-merge [& maps]
  (let [maps (filter identity maps)]
    (assert (every? map? maps))
    (apply merge-with deep-merge* maps)))
