
(ns bots.meteo.menu
  (:require
    ; [clojure.string :refer [trim lower-case] :as s]
    ; [taoensso.timbre :refer [warn]]
    ; [clj-time.core :as tc]
    ; [mlib.conf :refer [conf]]
    [mlib.telegram :as tg]
    [bots.meteo.commons :refer [apikey cid]]))

      ;  inkb q-st-alive
      ;  main-buttons locat-ll default-locat default-favs]]))

    ; [meteo.db :refer [st-near st-by-id st-find]]
    ; [bots.meteo.data :refer
    ;   [sess-params sess-save get-favs favs-add! favs-del!]]
    ; [bots.meteo.util :refer [format-st]]))
;


(defn cmd-menu [msg par]
  (let [stn 1]
    (tg/send-message apikey (cid msg)
      { :text "Настройки"
        :reply_markup
          {:inline_keyboard
            [
             [{:text "Все станции" :callback_data "all"}
              {:text "Избранное"   :callback_data "favs"}]
             [{:text "Рассылки"    :callback_data "subs"}
              {:text "Добавить"    :callback_data "adds"}]]}})))
;

;;.
