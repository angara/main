

(ns tourserv.html
  (:require
    [mlib.log :refer [debug info warn]]
    [mlib.core :refer [hesc]]
    ;
    [html.frame :refer [render]]
    [tourserv.const :refer [TYPE_MAP TOWNS TOWN_MAP]]
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
          :page-title (:title type)}
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
          tsrv (tourserv-by-type (:id type) (:id town))]

      (render req
        { :title (str "Турсервис - " (:title type) " - " (:title town))
          :page-title (str (:title type) " / " (:title town))}

        [:div.b-tourserv
          (for [t tsrv]
            [:div.b-tserv.col-md-6
              [:div.title (hesc (:title t))]
              [:div.descr (hesc (:descr t))]

              (when-let [addr (:addr t)]
                [:div.addr [:b "Адрес: "] (hesc addr)])
              (when-let [price (:price t)]
                [:div.price [:b "Цена: "] (hesc price)])
              (when-let [p (:email t)]
                [:div.email [:b "E-mail: "] (hesc p)])

              (when-let [p (:phone t)]
                [:div.phone
                  ;[:b "Телефон: "] (hesc p)
                  [:i.fa.fa-phone-square] "&nbsp; " (hesc p)
                  (when-let [p (:person t)]
                    [:span " &nbsp;" [:i.fa.fa-user-circle ] " " (hesc p)])])

              [:hr]])

          [:div.clearfix]]))))

        ;
        ; [:div.text-center {:style ""}
        ;   (for [t TOWNS :let [recs ()]])
        ;
        ;   (for [s tsrv]
        ;     [:div (:town s)])]))))
;


(defn serv-page [{params :params :as req}]
  (when-let [type (TYPE_MAP (:type params))]
    (let [tsrv (tourserv-by-type (:id type))]

      (render req
        { :title (str "Турсервис - " (:title type))
          :page-title (:title type)}
        ;
        [:div.b-tourserv
          "test serv-page"
          [:div.clearfix]]))))

;


(defn index-page [req]
  (render req
    { :title "Турсервис"
      :page-title "Турсервис"}
    ;
    [:div.text-center {:style "margin: 10ex;"}
      "Раздел на реконструкции."]))
;


;;.
