
(ns calendar.my
  (:require
    [mlib.log :refer [debug info warn]]
    [mlib.core :refer [hesc]]
    [mlib.time :refer [ddmmyyyy]]
    ;
    [html.frame :refer [render]]
    [calendar.db :refer [recs-by-uid]]))
;


(defn my-page [req]
  (let [uid (-> req :user :id)
        recs (recs-by-uid uid)]

    (render req
      {
        :page-title "Календарь моих событий"
        :css ["/incs/datepicker/datepicker.min.css"]
        :js  ["/incs/datepicker/datepicker.min.js"
              "/incs/calendar/my.js"]}
      ;
      [:div.b-calendar
        (if recs
          [:div.b-my
            (for [r recs]
              [:div.b-crec {:data-id (:_id r)}
                ;; [:div.col-sm-2.text-center
                [:a {:href (:link r)}
                  [:img.thumb {:src (:thumb r)}]]
                [:div.row
                    
                  [:div.col-sm-10
                    [:div
                      [:input.date
                        {:type "text" :value (ddmmyyyy (:date r))}]
                      [:span.status (:status r)]]
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

;;.
