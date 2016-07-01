

(ns bots.meteo.commands
  (:require
    [clojure.string :refer [trim lower-case] :as s]
    [taoensso.timbre :refer [warn]]
    [clj-time.core :as tc]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]
    [mlib.telegram :as tg]
    [meteo.db :refer [st-near st-by-id st-find]]
    [bots.meteo.data :refer
      [sess-params sess-save get-favs favs-add! favs-del!]]
    [bots.meteo.util :refer [format-st]]))
;

(defstate apikey
  :start
    (-> conf :bots :meteo38bot :apikey))
;


(defonce inline-kbd-serial (atom 0))

;; telegram message update workaround
(defn inkb [] (swap! inline-kbd-serial inc))


(def buttons
  { :resize_keyboard true
    :keyboard
      [[{:text "Погода"}
        {:text "Рядом" :request_location true}
        {:text "Меню"}]]})
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
    {:text "
По кнопке *Погода* выводятся посление данные с выбранных метеостанций.

Кнопка *Рядом* использует функцию геолокации для поиска ближайших станций.

В разделе *Меню* настройки списка избранных станций и управление рассылками.

Для поиска станции по названию или адресу отправьте текстовое сообщение длиной не менее трех букв.
"
     :parse_mode "Markdown"
     :reply_markup buttons}))
;


(defn st-kbd [st-id fav? more?]
  (let [k-fav
          { :text (str (if fav? "[*]" "[ ]") " Избранное")
            :callback_data
              (str (if fav? "favs-del " "favs-add ") st-id " " (inkb))}
        k-more
          {:text "Еще ..." :callback_data "more"}]
    {:reply_markup
      {:inline_keyboard
        (if more?
          [[k-fav k-more]]
          [[k-fav]])}}))
;


(defn has-more? [cid]
  (seq (:sts (sess-params cid))))

(defn next-st [cid sts]
  (when-let [sts (or sts (:sts (sess-params cid)))]
    (let [favs (get-favs cid)
          st   (first sts)
          st   (if (map? st) st (st-by-id st))
          id   (:_id st)
          tail (next sts)
          kbd  (st-kbd id (some #{id} favs) (seq tail))
          par  {:text (format-st st) :parse_mode "Markdown"}]
      (sess-save cid {:sts tail})
      (tg/send-message apikey cid (merge par kbd)))))
;


(defn cmd-all [msg par]
  (let [cid (cid msg)
        ids (map :_id (st-find (q-st-alive) [:_id]))]
    (when (seq ids)
      (tg/send-text apikey cid "Все станции:")
      (next-st cid ids))))
;

(defn cmd-near [msg par]
  (let [locat (:locat (sess-params (cid msg)) (default-locat))
        sts   (st-near (locat-ll locat) (q-st-alive))]
    (when (seq sts)
      (tg/send-text apikey cid "Ближайшие:")
      (next-st (cid msg) sts))))
;

(defn cmd-favs [msg par]
  (let [cid (cid msg)
        favs (or (not-empty (get-favs cid)) par)]
    (tg/send-text apikey cid "Избранные:")
    (next-st cid favs)))
;


(defn cmd-show [msg]
  (let [cid (cid msg)
        favs (or (not-empty (get-favs cid)) (default-favs))]
    (doseq [f favs]
      (when-let [st (st-by-id f)]
        (tg/send-message apikey cid
          {:text (format-st st) :parse_mode "Markdown"})))))
;

(defn cmd-subs [msg par]
  (let [cid (cid msg)])

  (prn "subs:" par))
;

(defn cmd-menu [msg par]
  (tg/send-message apikey (cid msg)
    { :text "Настройки"
      :reply_markup
        {:inline_keyboard
          [
           [{:text "Все станции"       :callback_data "all"}]
           [{:text "Избранные"         :callback_data "favs"}]
           [{:text "Список рассылок"   :callback_data "subs"}]
           [{:text "Добавить рассылку" :callback_data "adds"}]]}}))
;

(defn st-search [msg txt]
  (let [fnm (fn [stn]
                (let [nm (-> stn :title str lower-case)
                      ad (-> stn :addr  str lower-case)
                      ds (-> stn :descr str lower-case)]
                  (or
                    (.contains nm txt)
                    (.contains ad txt)
                    (.contains ds txt))))
        locat  (:locat (sess-params (cid msg)) (default-locat))
        sts (filter fnm (st-near (locat-ll locat) (q-st-alive)))]
    (if (seq sts)
      (next-st (cid msg) sts)
      (tg/send-text apikey (cid msg) "Станции не найдены.\n/help" true))))
;

(defn parse-command [text]
  (when-let [match (re-matches #"^/([A-Za-z0-9]+)([ _]+(.+))?$" text)]
    [(second match) (get match 3)]))
;

(defn on-message [msg]
  (let [text (-> msg :text str trim not-empty)
        [cmd par] (when text (parse-command text))
        locat (:location msg)]
    (cond
      cmd
        (condp = (lower-case cmd)
          "start" (cmd-help msg par)  ;; NOTE: change text?
          "help"  (cmd-help msg par)
          "all"   (cmd-all  msg par)
          "near"  (cmd-near msg par)
          "favs"  (cmd-favs msg nil)
          "subs"  (cmd-subs msg par)
                  (cmd-help msg nil))
      text
        (let [txt (lower-case text)]
          (cond
            (= "погода" txt) (cmd-show msg)
            (= "меню"   txt) (cmd-menu msg nil)
            :else  (if (<= 3 (.length text))
                      (st-search msg txt)
                      (cmd-help msg nil))))
      locat
        (do
          ;; TODO: save locat history
          (sess-save (cid msg) {:locat locat})
          (cmd-near msg nil))
      :else
        nil)))
;

(defn on-callback [cbq]
  (let [msg (:message cbq)
        cid (cid msg)
        [cmd par & params] (-> cbq :data str (s/split #"\s+"))]
    (when-not
      (condp = cmd
        "more" (do
                  ; (tg/api apikey :editMessageReplyMarkup
                  ;     {:chat_id cid :message_id (:message_id msg)})
                  (next-st cid nil)
                  nil)
        "favs-add"    ;; TODO: limit favs num
                  (do
                    (favs-add! cid par)
                    (tg/api apikey :editMessageReplyMarkup
                      (merge
                        {:chat_id cid :message_id (:message_id msg)}
                        (st-kbd par true (has-more? cid)))))    ;; check is added
        "favs-del"
                  (do
                    (favs-del! cid par)
                    (tg/api apikey :editMessageReplyMarkup
                      (merge
                        {:chat_id cid :message_id (:message_id msg)}
                        (st-kbd par false (has-more? cid)))))
        "all"   (do
                  (cmd-all msg nil)
                  nil)
        "favs"  (do
                  (cmd-favs msg nil)
                  nil)
        "subs" nil
        "adds" nil
        (warn "cbq-unexpected:" cmd))
      ;
      (tg/api apikey :answerCallbackQuery
        {:callback_query_id (:id cbq) :text ""}))))
;

;;.
