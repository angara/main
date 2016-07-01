
(ns bots.meteo.menu
  (:require
    [clojure.string :as s]
    ; [taoensso.timbre :refer [warn]]
    ; [clj-time.core :as tc]
    ; [mlib.conf :refer [conf]]
    [mlib.telegram :as tg]
    [bots.meteo.commons :refer [apikey cid gmaps-link]]

      ;  inkb q-st-alive
      ;  main-buttons locat-ll default-locat default-favs]]))

    [meteo.db :refer [st-ids]]
    [bots.meteo.data :refer [get-favs]]))
    ;   [sess-params sess-save get-favs favs-add! favs-del!]]
    ; [bots.meteo.util :refer [format-st]]))
;


(defn sect-favs [ids]
  (when-let [sts (not-empty (st-ids ids [:_id :title :ll]))]
    (str "В избранном:\n"
      (s/join ""
        (for [st sts
              :let [t (:title st) url (gmaps-link (:ll st))]]
          (str " [" t "](" url ")\n")))
      "\n")))
;

(defn cmd-menu [msg par]
  (let [cid (cid msg)
        ids (get-favs cid)]
    (tg/send-message apikey cid
      { :text (str
                (sect-favs ids)
                "Настройки")
        :parse_mode "Markdown"
        :reply_markup
          {:inline_keyboard
            [
             [{:text "Все станции" :callback_data "all"}
              {:text "Избранное"   :callback_data "favs"}]
             [{:text "Рассылки"    :callback_data "subs"}
              {:text "Добавить"    :callback_data "adds"}]]}})))
;

;;.
