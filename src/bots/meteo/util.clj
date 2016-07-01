
(ns bots.meteo.util
  (:require
    ; [clojure.string :refer [trim lower-case] :as s]
    ; [taoensso.timbre :refer [warn]]
    [clj-time.core :as tc]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]))
    ; [mlib.telegram :as tg]
    ; [meteo.db :refer [st-near st-by-id st-find]]
    ; [bots.meteo.data :refer
    ;   [sess-params sess-save get-favs favs-add! favs-del!]]
    ; [bots.meteo.util :refer [format-st]]))
;

(defstate apikey
  :start
    (-> conf :bots :meteo38bot :apikey))
;

(def wd-map
  {\0 "вс" \1 "пн" \2 "вт" \3 "ср" \4 "чт" \5 "пт" \6 "сб"})
;

(defn md-link [text url]
  (str "[" text "](" url ")"))
;

(defn gmaps-link [ll & [{z :z t :t}]]
  (let [c (str (second ll) "," (first ll))]
    (str "https://maps.google.com/maps?"
          "&q=loc:" c "&ll=" c "&t=" (or t "h") "&z=" (or z "18"))))
  ;; t = m,k,h,p
  ;; https://moz.com/blog/new-google-maps-url-parameters
;

(defonce inline-kbd-serial (atom 0))

;; telegram message update workaround
(defn inkb [] (swap! inline-kbd-serial inc))


(def st-alive-days 30)

(defn q-st-alive []
  { :pub 1
    :ts {:$gte (tc/minus (tc/now) (tc/days st-alive-days))}})
;

(defn default-locat []
  {:latitude 52.27 :longitude 104.27})
;

(defn default-favs []
  ["irgp" "asbtv" "uiii" "lin_list" "npsd" "zbereg" "olha"])
;

(defn cid [msg]
  (-> msg :chat :id))
;

(defn locat-ll [locat]
  [(:longitude locat) (:latitude locat)])
;


(def main-buttons
  { :resize_keyboard true
    :keyboard
      [[{:text "Погода"}
        {:text "Рядом" :request_location true}
        {:text "Меню"}]]})
;

;;.


; (def ic-havy-check        "\u2714")   ;; ✔
; (def ic-white-havy-check  "\u2705")   ;; ✅
; (def ic-havy-plus         "\u2795")   ;; ➕
; (def ic-havy-minus        "\u2796")   ;; ➖
; (def ic-glowing-star      "\uD83C\uDF1F")   ;; 🌟
