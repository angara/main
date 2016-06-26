(ns front.core
  (:require
    [taoensso.timbre :refer [warn]]
    [mlib.conf :refer [conf]]
    [html.frame :refer [layout]]))
;

(defn main-page [req]
  (layout req {}
    [:.uk-panel.uk-panel-box.uk-panel-space.uk-panel-header
      [:.uk-panel-badge.uk-badge "hi!"]
      [:h3.uk-panel-title "Angara.Net main page"]
      [:p
        "some text"]

      [:p
        "Главная События Информация Карты Снаряжение Турсервис Объявления Погода Фото Форум"]]))
;

;;.
