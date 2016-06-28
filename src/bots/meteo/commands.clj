

(ns bots.meteo.commands
  (:require
    [clojure.string :refer [trim lower-case]]
    [taoensso.timbre :refer [warn]]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]
;    [mlib.core :refer [to-int]]
    [mlib.telegram :as tg]))
;

(defstate apikey
  :start
    (-> conf :bots :meteo38bot :apikey))
;


(def buttons
  { :resize_keyboard true
    :keyboard
      [[{:text "Рядом" :request_location true}   ;; triangular_flag_on_post
        {:text "Мои"}       ;; :star: :star2:
        {:text "Меню"}]]})  ;; gear
;

(defn cmd-help [msg par]
  (prn "help:" par)
  (tg/send-message apikey (-> msg :chat :id)
    {:text "!!! Хелп текст должен быть здесь!!!"
     :parse_mode "Markdown"
     :reply_markup buttons}))
;

(defn cmd-all [msg par]
  (prn "all:" par))
;

(defn cmd-near [msg par]
  (prn "near:" par msg))
;

(defn cmd-favs [msg par]
  (prn "favs:" par))
;

(defn cmd-subs [msg par]
  (prn "subs:" par))
;

(defn cmd-menu [msg par]
  (prn "menu:" par))
;



(defn parse-command [text]
  (when-let [match (re-matches #"^/([A-Za-z0-9]+)([ _]+(.+))?$" text)]
    [(second match) (get match 3)]))
;

(defn on-message [msg]
  (prn "msg:" msg)
  (let [text (-> msg :text str trim)]
    (if-let [[cmd par] (parse-command text)]
      (condp = (lower-case cmd)
        "start" (cmd-help msg par)  ;; NOTE: change text?
        "help"  (cmd-help msg par)
        "near"  (cmd-near msg par)
        "favs"  (cmd-favs msg par)
        "subs"  (cmd-subs msg par)
                (cmd-help msg nil))
      (condp =  (lower-case text)
        ; "станции"   (cmd-all  msg nil)
        "рядом"     (cmd-near msg nil)
        "мои"       (cmd-favs msg nil)
        "меню"      (cmd-menu msg nil)
                    (cmd-help msg nil)))))
;

(defn on-callback [cbq]
  (prn "cbq:" cbq))
;

;;.
