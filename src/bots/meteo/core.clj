
(ns bots.meteo.core
  (:require
    [bots.meteo.data]
    [bots.meteo.poll]))
;

;; BotFather:
;
; /newobt meteo38bto
; /setabtoutext  Информация о погоде в Прибайкалье в реальном времени.
; /setuserpic photo: meteo38_icon_white.png
; /setdescription
;     Здесь можно получать данные о погоде со станций
;     проекта http://meteo38.ru, настроить
;     автоматическое уведомление в нужное время.
; /setcommands
;     near - ближайшие станции
;     favs - выбранные станции
;     subs - добавить выбранные в рассылку

;;;; !!!!  help - краткое пояснение
;;;; !!!   all  - список станций
