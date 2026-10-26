(ns auth.util
  (:require
    [clojure.string :refer [split trim lower-case] :as s]
    [taoensso.telemere :refer [log!]]
    [mlib.core :refer [email? cap-first]]
    [auth.bcrypt :refer [hash-pass]]
    [sql.user :refer [user-by-auth user-update add-auth-key]]
    ,))


(defn req-ip [req]
  (or (:real-ip req) (:remote-addr req)))


(defn norm-email [eml]
  (when-let
    [e (-> eml str trim lower-case not-empty)]
    (let [[user domain] (split e #"@" 2)
          [user plus-tail] (split user #"\+" 2)     ;; remove username+parts
          user (if (s/blank? user)                  ;; but keep +name parts
                 (str "+" plus-tail)
                 user)
          user (s/replace user #"\." "")    ;; no dots
          n (str user "@" domain)]
      (when (email? n)
        n))))


;; [2-32]
(def UN_RE #"[a-z]([a-z0-9_\.\-]){1,31}")
(def UN_DD #"[_\.\-]{2,}")
(def UN_ED #"[_\.\-]$")

;; 2-40 letters
(def NM_RE #"^(?iu)[a-zа-яЁё][a-zа-яЁё\-]{1,39}$")


(defn norm-username [un]
  (when-let
    [u (-> un str trim lower-case not-empty)]
    (when
      (and
        (re-matches UN_RE u)
        (not (re-find UN_DD u))
        (not (re-find UN_ED u)))
      u)))


(defn norm-first_name [s]
  (when-let
    [fname (-> s str trim not-empty)]
    (when
      (and
        (re-matches NM_RE fname)
        (not (re-find UN_DD fname))
        (not (re-find UN_ED fname)))
      (cap-first fname))))


(def norm-last_name norm-first_name)


(comment
  (norm-email "Mm.Aa+www@mm.dom")   ;; mmaa@mm.dom
  (norm-email "2@2.2")              ;; nil
  (norm-email "+2@me.me")           ;; +2@me.me

  (norm-username "UuSsEr")      ;; "uusser"
  (norm-username "UuSs.-Er"))   ;; nil


(defn em-norm [s]
  (when-let [em (norm-email s)]
    (str "em:" em)))


(defn un-norm [s]
  (when-let [un (norm-username s)]
    (str "un:" un)))


(defn migrate-user [sql-user]
  ;; legacy webapp row (plain-text password, no user_auth keys):
  ;; activate it for the new auth, entirely in postgres
  (let [uid (:uid sql-user)
        un (norm-username (:login sql-user))
        em (norm-email (:email sql-user))
        auth (vec (remove nil? [(when un (str "un:" un))
                                (when em (str "em:" em))]))
        passhash (or (:passhash sql-user)
                     (hash-pass (:password sql-user)))]
    (when (and passhash
               (user-update uid {:passhash passhash}))
      (doseq [a auth]
        (add-auth-key uid a))
      (if (every? #(= (str uid) (:id (user-by-auth % nil))) auth)
        {:id (str uid)
         :email (:email sql-user)
         :username (:login sql-user)
         :passhash passhash}
        ;;
        (do
          (log! :warn ["migrate-user: auth key conflict, uid" uid])
          nil)))))
