
(ns bots.meteo.core
  (:require
    [taoensso.timbre :refer [warn debug]]
    [mount.core :refer [defstate]]
    [mlib.conf :refer [conf]]
    [mlib.core :refer [to-int]]
    [mlib.telegram :as tg]))
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


(def api-error-sleep 3000)


(defn dispatch-update [upd]
  (try

    (prn "upd:" upd)

    (catch Exception e
      (warn "dispatch:" upd (.getMessage e)))))
;


(defn updates-loop [cnf]
  (let [token (:apikey cnf)
        poll-limit (:poll-limit cnf 100)
        poll-timeout (:poll-timeout cnf 1)]
    (loop [last-id 0  updates nil]
      (if-let [u (first updates)]
        (let [id (-> u :update_id to-int)]
          (if (< last-id id)
            (dispatch-update u)
            (debug "update-dupe:" id))
          (recur id (next updates)))
        ;
        (if-let [upd (tg/api token :getUpdates
                                  { :offset (inc last-id)
                                    :limit poll-limit
                                    :timeout poll-timeout})]
          (recur last-id upd)
          (do
            (warn "api-error")
            (Thread/sleep api-error-sleep)
            (recur last-id nil)))))))
;


(defn bot-loop []
  (updates-loop (-> conf :bots :meteo38bot)))

;;.
