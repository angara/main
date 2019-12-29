(ns bz.lib.util
  (:import
    [java.util Date]
    [java.time LocalDateTime Instant]
    [java.net URLEncoder]
    [java.util Base64])
  (:require
    [clojure.spec.alpha :as s]
    [clojure.string :refer [escape blank?] :as str]
    [clojure.set :refer [difference]]
    ;[clojure.java.io :as io]
    ;
    [jsonista.core  :as json]
    ;
    [bz.spec.const  :refer [E_PARAM]]))
;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

(def PCRE_SPECIALS ".^$*+?()[{|\\")
(def PCRE_ESCAPE_MAP
  (zipmap PCRE_SPECIALS (map #(str "\\" %) PCRE_SPECIALS)))

(defn pcre-escape [s]
  (escape s PCRE_ESCAPE_MAP))

(comment
  (pcre-escape "[")   "\\["
  (pcre-escape ".")   "\\."
  (pcre-escape "]")   "]"

  .)

; - - - - - - - - - - - - - -

(def json-read-keyword-mapper
  (json/object-mapper {:decode-key-fn true}))

(defn parse-json-string [^String s]
  (json/read-value s json-read-keyword-mapper))
;;

; - - - - - - - - - - - - - -

(defn base64->bytes [^bytes data]
  (.decode (Base64/getDecoder) data))
;;

(defn bytes->base64 [^bytes data]
   (.encode (Base64/getEncoder) data))
;;

(defn base64-decode [^String s]
  (String. (.decode (Base64/getDecoder) s)))
;;

(defn base64-encode [^String s]
  (.encodeToString (Base64/getEncoder) (.getBytes s)))
;;

;; ;; ;; ;; ;; ;; ;; ;; ;; ;;

; (defn urand-bytes [n]
;   (with-open [in (io/input-stream (io/file "/dev/urandom"))]
;     (let [buff (byte-array n)
;           _nrd (.read in buff)]
;       buff)))
; ;

(defn ex-chain [^Exception e]
  (take-while some? (iterate ex-cause e)))
;

(defn explain-problems [spec data]
  (->
    (s/explain-data spec data)
    (::s/problems)
    (first)
    (select-keys [:path :pred])))
;

(defn throw-err! [message code data]
  (throw
    (ex-info message (assoc data :code code))))
;

; (def not-blank?
;   (complement blank?))
;

(defn not-blank-string? [s]
  (and
    (string? s)
    (not (blank? s))))
;

(defn instant? [dt]
  (or
    (instance? LocalDateTime dt)
    (instance? Instant dt)
    (instance? Date dt)))
;

(def spec-keys-set
  (memoize
    (fn [spec]
      (let [km (->>
                  (s/describe spec)
                  (rest)
                  (apply hash-map))]
        (->>
          (concat (:req-un km) (:opt-un km))
          (map (comp keyword name))
          (set))))))
;

;; NOTE: https://github.com/bhb/expound

(defn conform-spec! [spec params]
  (let [extra-keys (difference
                      (-> params keys set)
                      (spec-keys-set spec))]
    (when-not (empty? extra-keys)
      (throw-err! "unexpected parameter" E_PARAM {:keys extra-keys})))
  ;
  (let [p (s/conform spec params)]
    (when (= ::s/invalid p)
      (throw-err! "invalid parameter" E_PARAM {:problems (explain-problems spec params)}))
    p))
;

(defn conform-field! [spec field params]
  (let [val (get params field)
        res (s/conform spec val)]
    (when (= res ::s/invalid)
      (throw-err! "wrong field value" E_PARAM {:field field :value val}))
    res))
;

; - - - - - - - - - - - - -

;; application/x-www-form-urlencoded

(defn urlencode [^String s]
  (URLEncoder/encode s "UTF-8"))
;;

(defn ^:private kwr-str [x]
  (if (keyword? x) (name x) (str x)))
;;

(defn urlencode-kv [obj]
  (->> obj
    (map
      (fn [[k v]]
        (str (urlencode (kwr-str k)) "=" (urlencode (kwr-str v)))))
    (str/join "&")))
;;

; - - - - - - - - - - - - -)

(defn json-body [resp]
  (let [ctype (get-in resp [:headers "content-type"])
        body (:body resp)]
    (if (and (string? body) (string? ctype) (.startsWith ctype "application/json"))
      (assoc resp :body (parse-json-string body))
      resp)))
;;

;;.
