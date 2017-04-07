

(ns tourserv.html
  (:require
    [mlib.log :refer [debug info warn]]
    [mlib.core :refer [hesc]]
    ;
    [html.frame :refer [render]]
    [tourserv.const :refer [TYPES TYPE_MAP TOWNS TOWN_MAP]]
    [tourserv.db :refer [tourserv-by-type]]))
;

(defn url-serv [id]
  (str "/tourserv/" id))
;

(defn url-town [twn]
  (str "/tourserv/apart/" (name twn)))
;


(def APARTS_TOP_NUM 4)

(defn serv-apart [req]
  (let [type (TYPE_MAP "apart")
        aparts (group-by :town (tourserv-by-type (:id type)))]
    ;
      (render req
        { :title (str "Турсервис - " (:title type))
          :page-title (:title type)
          :topmenu :tourserv}
        ;
        [:div.b-tourserv
          (for [t TOWNS
                :let [recs (get aparts (:id t))
                      has-more (< APARTS_TOP_NUM (count recs))
                      recs (take APARTS_TOP_NUM recs)]
                :when recs]
            [:div.col-sm-4
              [:a {:href (url-town (:id t))}
                [:h4.town-title (:title t)]]
              [:ul {:style "margin-right: 1rem;"}
                (for [r recs]
                  [:li
                    [:a {:href (url-serv (:_id r))}
                      (hesc (:title r))]])
                (when has-more
                  [:div.text-right
                    [:a { :href (url-town (:id t))
                          :style "margin-right: 2em;"}
                      [:i.fa.fa-ellipsis-h]]])]])

          [:div.clearfix]])))
;

(defn serv-town [{params :params :as req}]
  (when-let [town (TOWN_MAP (:town params))]
    (let [type (TYPE_MAP "apart")
          twn_id (:id town)
          tsrv (tourserv-by-type (:id type) twn_id)]

      (render req
        { :title (str "Турсервис / " (:title type) " / " (:title town))
          :page-title (str (:title type) ": " (:title town))
          :topmenu :tourserv}
        ;
        [:div.b-tourserv
          [:ol.breadcrumb
            (for [twn TOWNS
                  :let [tid (:id twn)
                        cls (when (= tid twn_id) "active")]]
              [:li {:class cls}
                [:a {:href (url-town tid)} (:title twn)]])]

          (for [t tsrv]
            [:div.b-tserv.col-md-6
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
                [:div.person [:i.fa.fa-user-circle.fa-fw] (hesc p)])

              [:hr]])

          [:div.clearfix]]))))
;


(defn serv-page [{params :params :as req}]
  (when-let [type (TYPE_MAP (:type params))]
    (let [tsrv (tourserv-by-type (:id type))]

      (render req
        { :title (str "Турсервис / " (:title type))
          :page-title (:title type)
          :topmenu :tourserv}
        ;
        [:div.b-tourserv
          "test serv-page"
          [:div.clearfix]]))))

;


(defn index-page [req]
  (render req
    { :title "Турсервис"
      ; :page-title "Турсервис"
      :topmenu :tourserv}
    ;
    [:div.b-tourserv
      [:div.col-md-6.col-md-offset-2.b-index
        (for [t TYPES]
          [:div.tsrv
            [:a {:href (str "/tourserv/" (:id t))}
              [:h3 (:title t)]]
            (:descr t)])]

      [:div.clearfix]]))
;


;;.
