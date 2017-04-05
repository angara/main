

(ns tourserv.html
  (:require
    [mlib.log :refer [debug info warn]]
    [mlib.core :refer [hesc]]
    ;
    [html.frame :refer [render]]
    [tourserv.const :refer [TYPE_MAP TOWNS TOWN_MAP]]
    [tourserv.db :refer [tourserv-by-type]]))
;


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
                :let [recs (get aparts (:id t))]
                :when recs]
            [:div.col-sm-4
              [:h3 (:title t)]])

          [:div.clearfix]])))
;

(defn serv-town [{params :params :as req}]
  (when-let [type (TYPE_MAP (:type params))]
    (let [tsrv (tourserv-by-type (:id type))
          grouped-by-town (group-by :town tsrv)]

      (render req
        { :title (str "Турсервис - " (:title type))
          :page-title (:title type)}

        [:div.b-tourserv
          "test"
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
