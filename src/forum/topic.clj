

(ns forum.topic
  (:require
    [clojure.string :as s]
    [clj-time.core :as tc]
    [honeysql.helpers :as h]
    [compojure.core :refer [GET POST ANY defroutes]]
    ;
    [mlib.log :refer [warn]]
    [mlib.http :refer [json-resp]]
    [mlib.core :refer [to-int]]
    [sql.core :refer [fetch exec]]
    ;
    [forum.db :refer [FORUM_TOPICS FORUM_LASTREAD USERS]]))
;


(def ROLE_FORUM \F)
(def FORUM_EDIT_AGE (tc/days 60))

(def FIRST_CHARS
  (set "0123456789\"ABCDEFGHIJKLMNOPQRSTUVWXYZАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"))
;

(def STOP_STRS
  [
    ".."  ",," "??" "**" "--" "++" "==" "//"
    "((" "))" "[[" "]]" "{{" "}}" "<<" ">>" "##" "@@" "$$"
    "~~" "``" "''" "&&" "__" "||" ";;" "::" "\"\"" "\\\\"])
;

(defn user-role-set [uid]
  (->
    (h/select :roles)
    (h/from USERS)
    (h/where [:= :uid uid])
    fetch first :roles str set))
;


(defn topic-notify [{user :user params :params}]
  (let [uid (-> user :id to-int)
        tid (-> params :tid to-int)
        nfy (-> params :nfy not-empty)]
    (when (and uid tid)
      (json-resp
        {:ok
          (->
            (h/update FORUM_LASTREAD)
            (h/sset {:watch (if nfy 1 0)})
            (h/where [:= :uid uid] [:= :tid tid])
            exec)}))))
;


(defn- valid-title? [title]
  (and
    (FIRST_CHARS (first title))
    (not (some #(s/includes? title %) STOP_STRS))))
;

(defn- correct-title [title]
  (let [title (reduce
                #(s/replace %1 (first %2) (second %2))
                title
                [ [#"\s+\."  "."]
                  [#"\s+\,"  ","]
                  [#"\s+\:"  ":"]
                  [#"\s+\;"  ";"]
                  [#"\s+\!"  "!"]
                  [#"\s+\?"  "?"]])]
    (when (<= 5 (.length title))
      (let [title (str (s/upper-case (subs title 0 1)) (subs title 1))]
        (when (valid-title? title)
          title)))))
;

(defn- update-title [tid title & where]
  (let [rc (->
              (apply h/where [:= :tid tid] where)
              (h/update FORUM_TOPICS)
              (h/sset {:title title})
              exec (= 1))]
    (if rc
      { :ok 1
        :title title}
      { :err :update_failed
        :msg "Невозможно изменить заголовок темы."})))
;

(defn topic-title [{user :user params :params}]
  (let [uid (-> user :id to-int)
        tid (-> params :tid to-int)]
    (when (and uid tid)
      (json-resp
        (if-let [title (-> params :title str s/trim correct-title)]
          (if ((user-role-set uid) ROLE_FORUM)
            (update-title tid title)
            (update-title tid title
              [:= :owner uid]
              [:= :closed false]
              [:> :created (tc/minus (tc/now) FORUM_EDIT_AGE)]))
          ;
          {:err :bad_data :msg "Недопустимый текст заголовока."})))))
          ;
        ;
;


(defn topic-state [{user :user params :params}]
  (let [uid (-> user :id to-int)
        tid (-> params :tid to-int)
        close (-> params :close (= "true"))]
    (when (and uid tid)
      (let [rc
              (->
                (h/update FORUM_TOPICS)
                (h/sset {:closed close})
                (h/where [:= :owner uid] [:= :tid tid])
                exec (= 1))]
        (json-resp
          (if rc
            {:ok 1 :tid tid :state (if close "closed" "opened")}
            {:err :not_found :msg "Тема не найдена."}))))))
;

(defroutes topic-routes
  (POST "/notify" [] topic-notify)
  (POST "/title"  [] topic-title)
  (POST "/state"  [] topic-state))  ;; {:tid tid :closed true|false}
;

;;.
