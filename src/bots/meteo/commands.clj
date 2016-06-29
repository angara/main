

(ns bots.meteo.commands
  (:require
    [clojure.string :refer [trim lower-case] :as s]
    [taoensso.timbre :refer [warn]]
    [clj-time.core :as tc]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]
    [mlib.telegram :as tg]
    [meteo.db :refer [st-near]]
    [bots.meteo.sess :as sess]))
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

(def st-alive-days 30)

(defn q-st-alive []
  { :pub 1
    :ts {:$gte (tc/minus (tc/now) (tc/days st-alive-days))}})
;

(defn default-locat []
  {:latitude 52.27 :longitude 104.27})


(defn cid [msg]
  (-> msg :chat :id))
;

(defn locat-ll [locat]
  [(:longitude locat) (:latitude locat)])
;

(defn cmd-help [msg par]
  (prn "help:" par)
  (tg/send-message apikey (cid msg)
    {:text "!!! Хелп текст должен быть здесь!!!"
     :parse_mode "Markdown"
     :reply_markup buttons}))
;

(defn cmd-all [msg par]
  (prn "all:" par))
;


(defn add-nl [s]
  (when s (str s "\n")))
;

(defn cmd-near [msg par]
  (let [locat  (:locat (sess/params (cid msg)) (default-locat))
        sts (st-near (locat-ll locat) (q-st-alive))]
    (prn "sts:" sts)
    (let [tx (for [x sts
                    :let [st (:obj x) dis (:dis x)]]
                (str "*" (:title st) "*" "\n"
                    (add-nl (:descr st))
                    (add-nl (:addr st))
                    (format "(%.1f км)" (/ dis 1000)) "\n"))]
      (tg/send-text apikey (cid msg)
        (s/join "\n" tx) true))))
    ;
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
        (condp =  (lower-case text)
          ; "станции"   (cmd-all  msg nil)
          "рядом"     (cmd-near msg nil)
          "мои"       (cmd-favs msg nil)
          "меню"      (cmd-menu msg nil)
                      (cmd-help msg nil))
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
