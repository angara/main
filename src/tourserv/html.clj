(ns tourserv.html
  (:require
    [mlib.config :refer [conf]]
    [mlib.core :refer [hesc]]
    ; [mlib.web.snippets :refer [ya-rtb]]
    ;
    [html.frame :refer [render-layout]]
    [tourserv.const :refer [TYPES TYPE_MAP TOWNS TOWN_MAP]]
    [tourserv.db :refer [tourserv-by-type]]))
;

; (defn url-serv [type srv_id]
;   (str "/tourserv/" type "#" srv_id))
; ;

(defn url-town [twn srv_id]
  (str "/tourserv/apart/" (name twn)
    (when srv_id (str "#" srv_id))))
;

(def APARTS_TOP_NUM 4)

(defn serv-apart [req]
  (let [type (TYPE_MAP "apart")
        aparts (group-by :town (tourserv-by-type (:id type)))]
    ;
      (render-layout req
        { :title (str "Турсервис - " (:title type))
          :page-title (:title type)
          :topmenu :tourserv
          :rtb-top (:rtb-top conf)
          :rtb-bottom (:rtb-bottom conf)}
        ;
        [:div.b-tourserv
          (for [t TOWNS
                :let [recs (get aparts (:id t))
                      has-more (< APARTS_TOP_NUM (count recs))
                      recs (take APARTS_TOP_NUM recs)]
                :when recs]
            [:div.col-sm-4
              [:a {:href (url-town (:id t) "")}
                [:h4.town-title (:title t)]]
              [:ul {:style "margin-right: 1rem;"}
                (for [r recs]
                  [:li
                    [:a {:href (url-town (:id t) (:_id r))}
                      (hesc (:title r))]])
                (when has-more
                  [:div.text-right
                    [:a { :href (url-town (:id t) nil)
                          :style "margin-right: 2em;"}
                      [:i.fa.fa-ellipsis-h]]])]])

          [:div.clearfix]])))
;

(defn serv-town [{params :params :as req}]
  (when-let [town (TOWN_MAP (:town params))]
    (let [type (TYPE_MAP "apart")
          twn_id (:id town)
          tsrv (tourserv-by-type (:id type) twn_id)]

      (render-layout req
        { :title (str "Турсервис / " (:title type) " / " (:title town))
          :page-title (str (:title type) ": " (:title town))
          :topmenu :tourserv
          :rtb-top (:rtb-top conf)
          :rtb-bottom (:rtb-bottom conf)}
        ;
        [:div.b-tourserv
          [:ol.breadcrumb
            (for [twn TOWNS
                  :let [tid (:id twn)
                        cls (when (= tid twn_id) "active")]]
              [:li {:class cls}
                [:a {:href (url-town tid nil)} (:title twn)]])]

          (for [t tsrv]
            [:div.b-tserv.col-md-6
              [:a {:name (:_id t)}]
              [:div.title (hesc (:title t))]
              [:div.descr (hesc (:descr t))]
              (when-let [price (:price t)]
                [:div.price [:b "Цена: "] (hesc price)])
              (when-let [addr (:addr t)]
                [:div.addr [:i.fa.fa-map-marker.fa-fw] (hesc addr)])
              (when-let [p (:email t)]
                [:div.email [:i.fa.fa-envelope.fa-fw] (hesc p)])
              (when-let [p (:phone t)]
                [:div.phone [:i.fa.fa-phone-square.fa-fw] (hesc p)])
              (when-let [p (:person t)]
                [:div.person
                  (if-let [link (:link t)]
                    [:a {:href link}
                      [:i.fa.fa-user-circle.fa-fw] (hesc p)]
                    [:span
                      [:i.fa.fa-user-circle.fa-fw] (hesc p)])])
                ; person
              [:hr]])

          [:div.clearfix]]))))
          ;; (ads)]))))
;


(defn serv-page [{params :params :as req}]
  (when-let [type (TYPE_MAP (:type params))]
    (let [tsrv (tourserv-by-type (:id type))]

      (render-layout req
        { :title (str "Турсервис / " (:title type))
          :page-title (:title type)
          :topmenu :tourserv
          :rtb-top (:rtb-top conf)
          :rtb-bottom (:rtb-bottom conf)}
        ;
        [:div.b-tourserv
          (for [t tsrv :let [flags (set (:flags t))]]
            [:div.b-tserv.col-md-6
              [:div.title (hesc (:title t))]
              [:div.descr
                (if (get flags "rawhtml")
                  (:descr t)
                  (hesc (:descr t)))]
              (when-let [p (:payload t)]
                [:div.payload [:b "Полезная нагрузка: "] (hesc p)])
              (when-let [price (:price t)]
                [:div.price [:b "Цена: "] (hesc price)])
              (when-let [addr (:addr t)]
                [:div.addr [:i.fa.fa-map-marker.fa-fw] (hesc addr)])
              (when-let [p (:email t)]
                [:div.email [:i.fa.fa-envelope.fa-fw] (hesc p)])
              (when-let [p (:phone t)]
                [:div.phone [:i.fa.fa-phone-square.fa-fw] (hesc p)])
              (when-let [p (:person t)]
                [:div.person
                  (if-let [link (:link t)]
                    [:a {:href link}
                      [:i.fa.fa-user-circle.fa-fw] (hesc p)]
                    [:span
                      [:i.fa.fa-user-circle.fa-fw] (hesc p)])])
              ; person

              [:hr]])

          [:div.clearfix]]))))
          ;; (ads)]))))
;


(defn index-page [req]
  (render-layout req
    { :title "Турсервис"
      ; :page-title "Турсервис"
      :topmenu :tourserv
      :rtb-top (:rtb-top conf)
      :rtb-bottom (:rtb-bottom conf)}
    ;
    [:div.b-tourserv
      [:div.col-md-7.b-index
        (for [t TYPES]
          [:div.tsrv
            [:a {:href (str "/tourserv/" (:id t))}
              [:h3 (:title t)]]
            (:descr t)])]
      [:div.col-md-4.col-md-offset-1
        ; [:hr]
        ; [:div
        ;   [:a {:href "/rest/pikcher/"} [:h4 "Турбаза Пик Черского"]]
        ;   [:a {:href "/rest/pikcher/"}
        ;     [:img
        ;       {:src "/rest/pikcher/logo_200.jpg"
        ;        :style "margin: 0 12px 4px 0; float:left;
        ;                 border: 1px solid #ccc; padding: 1px;"}]]
        ;   "<p>База расположена в верховьях реки Слюдянка,"
        ;   " которая впадает в озеро Байкал."
        ;   " Это предгольцовая зона с вековыми кедрами у подножья пика Черского,"
        ;   " на который проходит один из популярных маршрутов выходных дней "
        ;   "<a href='/rest/pikcher/'>...&raquo;&raquo;</a></p>"]
        ; [:div.clearfix]
        ; [:hr]

        [:div
          [:a {:href "/rest/hdaban/"} [:h4 "Турбаза Хамар-Дабан"]]
          [:a {:href "/rest/hdaban/"}
            [:img
              {:src "/rest/hdaban/logo-200.jpg"
               :style "margin: 0 12px 4px 0; float:left; border: 1px solid #ccc;"}]]
          "<p>Размещение в уютных домиках в одном из самых популярных мест"
          " Хамар-Дабана, недалеко от пика Черского.</p>"
          " <p>К Вашим услугам гиды-проводники, которые помогут организовать"
          " Ваше путешествие к пику Черского, на водопады реки Подкомарная"
          " или сплав по таким рекам как Снежная, Мурино, Утулик "
          "<a href='/rest/hdaban/'>...&raquo;&raquo;</a></p>"]
        [:div.clearfix]
        [:hr]]
        ;
      [:div.clearfix]]))
      ;; (ads)]))
;


; <div class="abody"><a name="pikcher"></a>
; <h5 class="bar cn"><a href="pikcher/">Турбаза Пик Черского</a></h5>
;
; <a href="pikcher/"><img src="pikcher/logo_200.jpg"
;  style="margin: 0px 16px 4px 8px; float: left; border: 1px solid #487c84;" /></a>
;
; <p>
; База расположена в верховьях реки Слюдянка, которая впадает в озеро Байкал.
; Это предгольцовая зона с вековыми кедрами у подножья пика Черского,
; на который проходит один из популярных маршрутов выходных дней.
; <nobr><a href="pikcher/">Подробнее ... &raquo;&raquo;</a></nobr>
; </p>
; </div>

; <div class="abody"><a name="hdaban"></a>
; <h5 class="bar cn"><a href="hdaban/">Турбаза Хамар-Дабан</a></h5>
; <a href="hdaban/"><img src="hdaban/logo.jpg"
;         style="margin: 0px 16px 4px 8px; float: left; width: 180px; height: 133px;
;         border: 1px solid #487c84;" /></a>
; <p>
; Размещение в уютных домиках в одном из самых популярных мест
; Хамар-Дабана, недалеко от пика Черского.</p>
; <p>
; К Вашим услугам гиды-проводники, которые помогут организовать Ваше путешествие к пику Черского, на
; водопады реки Подкомарная или сплав по таким рекам как Снежная, Мурино, Утулик.
; <nobr><a href="hdaban/">Подробнее ... &raquo;&raquo;</a></nobr></p>

;;.
