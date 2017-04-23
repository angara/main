
(ns calendar.my
  (:require
    [mlib.log :refer [debug info warn]]
    [mlib.core :refer [hesc]]
    [mlib.http :refer [json-resp]]
    [mlib.time :refer [ddmmyyyy parse-ddmmyyyy]]
    ;
    [mdb.core :refer [oid]]
    [html.frame :refer [render]]
    [calendar.db :refer [crecs-by-uid crec-by-id-uid crec-update]]))
;


(defn my-page [req]
  (let [uid (-> req :user :id)
        recs (crecs-by-uid uid)]
    ;
    (render req
      {
        :page-title "Календарь: мои события"
        :topmenu :calendar
        :css ["/incs/datepicker/datepicker.min.css"]
        :js  ["/incs/datepicker/datepicker.min.js"
              "/incs/calendar/my.js"]}
      ;
      [:div.b-calendar
        (if recs
          [:div.b-my
            (for [r recs
                  :let [status (-> r :status str)]]
              [:div.b-crec {:data-id (:_id r)}
                [:a {:href (:link r)}
                  [:img.thumb {:src (:thumb r)}]]
                [:div.row
                  [:div.col-sm-10
                    [:div
                      [:input.date
                        {:type "text" :value (ddmmyyyy (:date r))}]
                      " "
                      [:label.lbl-status
                        [:input.status
                          (if (not-empty status)
                            {:type "checkbox" :checked "1"}
                            {:type "checkbox"})]
                        " Показывать"]]
                    [:div
                      [:input.title {:type "text" :value (:title r)}]]]]
                [:div.clearfix]])]
          ;
          [:div.jumbotron {:style "margin-top: 1em;"}
            [:h2.text-center "Нет записей."]
            [:br][:br]
            [:p
              "Чтобы добавить событие в календарь, загрузите JPG файл
              в первое сообщение темы и нажмите ссылку
              <nobr>[в календарь]</nobr> в заголовке."]
            [:br][:br]])])))
;


(defn my-update [req]
  (let [par (:params req)
        id  (:id par)
        uid (-> req :user :id)
        crec (crec-by-id-uid id uid)]
    (if (and crec (not= "removed" (:status crec)))
      ;; TODO: validate params
      (let [date   (-> par :date parse-ddmmyyyy)
            title  (-> par :title)
            status (-> par :status)
            status (when-not (nil? status)
                      (if status "publ" ""))]
        ;
        (if-let [upd (cond-> nil
                        date   (assoc :date   date)
                        status (assoc :status status)
                        title  (assoc :title  title))]
          (if (crec-update id upd)
            (json-resp {:ok 1})
            (json-resp {:err :db}))
          ;;
          (json-resp {:err :params})))
        ;;
      ;
      (json-resp {:err :not_found :msg "Запись не найдена."}))))
;


;;.
