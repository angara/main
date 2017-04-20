
(ns calendar.core
  (:require
    [clojure.java.shell :refer [sh]]
    [clj-time.core :as tc]
    [compojure.core :refer [defroutes GET POST]]  ;context ANY
    ;
    [mlib.conf :refer [conf]]
    [mlib.core :refer [to-int]]
    [mlib.http :refer [json-resp]]
    ;
    [html.frame :refer [wrap-user-required]]
    [web.middleware :refer [wrap-throttle]]
    [calendar.db :refer [add-crec]]
    [calendar.html :refer [index-page]]
    [calendar.my :refer [my-page]]
    [forum.db :refer
      [get-topic get-messages attach-params]]))
;


(def URL_CALENDAR_MY "/calendar/my")

(def FRESH_TOPIC_AGE (tc/days 30))


(defn thumb-100 [orig dest]
  (when (and orig dest)
    (sh "convert" orig ;; "-auto-gamma" "-auto-level"
        "-auto-orient"
        "-filter" "Lanczos"
        "-unsharp" "0x1"
        "-thumbnail" "100x100^"
        "-gravity" "center"
        "-extent" "100x100"
        "-strip"
        "-quality" "80" dest)))
;

(defn topic-info [tid]
  (when-let [topic (get-topic tid)]
    (let [[msg msg1] (get-messages tid 0 2)
          att (or
                (attach-params msg)
                (attach-params msg1))]
      {
        :topic  topic
        :msg    msg
        :attach att})))
;

(defn tinfo-calendar [tinfo]
  (when tinfo
    (let [cnf (:forum conf)
          dir (:upload-dir cnf)
          {type :type base :base} (:attach tinfo)
          t100 (str base "_100.jpg")
          thumb (when (= "jpg" type)
                  (thumb-100
                    (str dir base ".jpg")
                    (str dir t100))
                  (str (:upload-uri cnf) t100))
          topic (:topic tinfo)]
      ;
      {
        :uid    (str (:owner topic))
        :title  (:title topic)
        :descr  (-> tinfo :msg :body)
        :link   (str "/forum/t" (:tid topic))
        :thumb  thumb})))
        ;; tags by tgroup ???
;


(defn add-tinfo [uid tinfo]
  (when-let [crec (tinfo-calendar tinfo)]
    (let [now   (tc/now)
          crec  (assoc crec
                  :uid uid :ct now :ts now
                  :date (tc/plus now (tc/days 1))    ;; lookup date in descr
                  :status nil)]
      (add-crec crec))))
;

(defn allow-add? [uid tinfo]
  (when-let [topic (:topic tinfo)]
    (cond
      ;; is admin
      (= "1" uid)
      true

      ;; no commercial topics
      (<= 1000 (:tid topic))
      false

      ;; no old topics
      (tc/before?
        (:created topic)
        (tc/minus (tc/now) FRESH_TOPIC_AGE))
      false

      ;; only by owner owner
      (not= uid (str (:owner topic)))
      false

      ;; title
      ;; attach ?

      :else true)))
;


(defn topic-check [{params :params :as req}]
  (let [uid (-> req :user :id)
        tid (-> params :tid to-int)]
    (if-let [tinfo (topic-info tid)]
      (json-resp
        {:ok 1 :allowed (allow-add? uid tinfo)})
      (json-resp
        {:err :not_found}))))
;

(defn topic-add [{params :params :as req}]
  (let [uid (-> req :user :id)
        tid (-> params :tid to-int)]
    (if-let [tinfo (topic-info tid)]
      (if (allow-add? uid tinfo)
        (if (add-tinfo uid tinfo)
          (json-resp
            {:ok 1 :redir URL_CALENDAR_MY})
          (json-resp
            {:err :syserr :msg "Ошибка при добавлении темы"}))
        ;
        (json-resp
          {:err :denied :msg "Невозможно добавить тему в календарь"}))
      ;
      (json-resp
        {:err :not_found}))))
;

(defroutes calendar-routes
  (GET  "/"           [] index-page)
  ;
  (GET  "/my"         [] (wrap-user-required my-page))
  ;
  (GET  "/add-topic"  [] topic-check)
  (POST "/add-topic"  []
          (wrap-throttle topic-add {:time 1000 :limit 2})))
;

;;
