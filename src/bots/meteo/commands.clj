

(ns bots.meteo.commands
  (:require
    [clojure.string :refer [trim lower-case] :as s]
    [taoensso.timbre :refer [warn]]
    [clj-time.core :as tc]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]
    [mlib.telegram :as tg]
    [meteo.db :refer [st-near st-by-id]]
    [bots.meteo.sess :as sess]
    [bots.meteo.util :refer [format-st]]))
;

(defstate apikey
  :start
    (-> conf :bots :meteo38bot :apikey))
;


(def buttons
  { :resize_keyboard true
    :keyboard
      [[{:text "Погода"}; :request_location true}   ;; triangular_flag_on_post
        {:text "Меню"}]]})  ;; gear
;

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

(defn cmd-help [msg par]
  (tg/send-message apikey (cid msg)
    {:text "!!! Хелп текст должен быть здесь!!!"
     :parse_mode "Markdown"
     :reply_markup buttons}))
;


(defn cmd-near [msg par]
  (let [locat  (:locat (sess/params (cid msg)) (default-locat))
        sts (st-near (locat-ll locat) (q-st-alive))]
    (doseq [x sts]
      (tg/send-text apikey (cid msg) (format-st (:obj x) (:dis x)) true))))
    ;
;

(defn cmd-favs [msg par]
  (let [cid (cid msg)
        favs (:favs (sess/params cid) (default-favs))]
    (doseq [f favs]
      (tg/send-text apikey cid
        (format-st (st-by-id f))
        true))))
;

(defn cmd-subs [msg par]
  (prn "subs:" par))
;

(defn cmd-menu [msg par]
  (prn "menu:" par))
;

(defn st-search [msg txt])
;

(defn parse-command [text]
  (when-let [match (re-matches #"^/([A-Za-z0-9]+)([ _]+(.+))?$" text)]
    [(second match) (get match 3)]))
;

(defn on-message [msg]
  (prn "msg:" msg)
  (let [text (-> msg :text str trim not-empty)
        [cmd par] (when text (parse-command text))
        locat (:location msg)]
    (cond
      cmd
        (condp = (lower-case cmd)
          "start" (cmd-help msg par)  ;; NOTE: change text?
          "help"  (cmd-help msg par)
          "near"  (cmd-near msg par)
          "favs"  (cmd-favs msg par)
          "subs"  (cmd-subs msg par)
                  (cmd-help msg nil))
      text
        (let [txt (lower-case text)]
          (cond
            (= "погода" txt) (cmd-favs msg nil)
            (= "меню"   txt) (cmd-menu msg nil)
            :else   (or
                        (st-search msg txt)
                        (cmd-help msg nil))))
      locat
        (do
          ;; TODO: save locat history
          (sess/save (cid msg) {:locat locat})
          (cmd-near msg nil))
      :else
        nil)))
;

(defn on-callback [cbq]
  (prn "cbq:" cbq))
;

;;.
