
(ns html.util
  (:require
    [mlib.logger :refer [warn]]
    [mlib.http :as h]
    [mlib.web.snippets :as snip]))
;

;; rum.server-render
(defn inner-html [s]
  ;; [:span {:dangerouslySetInnerHTML {:__html s}}])
  s)
;


(defn script [js-file]
  [:script {:src js-file :type "text/javascript" :defer 1}])

(defn css-link [css-file]
   [:link {:href css-file :rel "stylesheet" :type "text/css"}])


(defn glyphicon [nm]
  [:span {:class (str "glyphicon glyphicon-" nm)}])

(defn form-label [for_id cls text]
  [:label {:for for_id :class (str "control-label " cls)} text])

(defn form-input [id value]
  [:input.form-control {:id id :type "text" :value value}])


(def json-resp  h/json-resp)

(defn json-err
  ([err] (h/json-resp {:err (str err)}))
  ([err msg] (h/json-resp {:err (str err) :msg (str msg)})))

(defn bad-req [] (json-err "req"))
(defn sys-err [] (json-err "sys"))
(defn ok-resp [] (json-resp {:ok 1}))


; (defn- fb-oauth-uri [nextpage]
;     (let [cfg (:facebook config)]
;         (str "https://www.facebook.com/dialog/oauth"
;             "?client_id=" (:appid cfg)
;             "&redirect_uri=" (:redirect cfg)
;             "&response_type=code"
;             "&state=" nextpage
;         )
;     ))


; (defn identicon [u s]
;   (str "http://www.gravatar.com/avatar/" (digest/md5 (str u)) "?d=identicon&s=" s))


; (defn- req-url [req]
;   (let [qs (:query-string req)]
;     (str (:uri req) (and qs (str "?" qs)))))
; ;

(defn req-tempfile [req file-param]
  (try
    (.getCanonicalPath (get-in req [:params file-param :tempfile]))
    (catch Exception e (warn "req-tempfile:" e))))


(defn ya-direct [{:keys [client stat type limit]
                  :or {client 1908 stat 1 type "horizontal" limit 2}}]
  (snip/ya-direct (str "yad_" stat) client stat type limit))

;;.
