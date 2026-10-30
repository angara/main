(ns meteo.favorites
  (:require
    [meteo.data :as data]
    [mlib.http :refer [json-resp]]
    [sql.meteo :as db]))

(defn favorites-first [stations favorites]
  ;; sort-by is stable: preserve the API order within both groups.
  (sort-by #(if (contains? favorites (:st %)) 0 1) stations))

(defn update-favorite [req]
  (let [uid (get-in req [:user :id])
        csrf (get-in req [:sess :_csrf])
        {:keys [st favorite]} (:params req)]
    (cond
      (not uid)
      (json-resp 401 {:err :auth :msg "Войдите на сайт."})

      (not (and (string? csrf) (seq csrf)
                (= csrf (get-in req [:headers "x-csrf-token"]))))
      (json-resp 403 {:err :csrf :msg "Обновите страницу и попробуйте снова."})

      (not (and (string? st) (<= 1 (count st) 100) (boolean? favorite)))
      (json-resp 400 {:err :param :msg "Неверные параметры станции."})

      ;; Removal must work even if a station is no longer active.
      (and favorite (not (some #(= st (:st %)) (data/active-stations))))
      (json-resp 400 {:err :station :msg "Станция недоступна. Обновите страницу."})

      (db/set-favorite! uid st favorite)
      (json-resp {:ok 1 :st st :favorite favorite})

      :else
      (json-resp 401 {:err :auth :msg "Войдите на сайт."}))))
