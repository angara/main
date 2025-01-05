(ns meteo.brief
  (:require
    [clj-time.core :as tc]
    ; 
    [app.config        :refer [conf]]
    [mlib.core         :refer [hesc]]
    [mlib.time         :refer [hhmm]]
    [mlib.web.snippets :refer [ya-rtb]]
    ; ;
    [meteo.db          :refer [st-ids]]
    [meteo.fmt         :refer [format-t format-h format-p format-w format-wt]]
   ,))


(def ST_DEAD_INTERVAL (tc/hours 8))   ;; duplicated in index but different
(def ST_BASE_URL      "/meteo/st/")   ;; duplicated in index

(defn fresh-st-data []
  (let [st-list (-> conf :main :meteo :st_default)
        t0  (tc/minus (tc/now) ST_DEAD_INTERVAL)]
    (->>
      (st-ids st-list)
      (keep
        #(when-let [ts (-> % :last :ts)]
          (when (tc/after? ts t0)
            %)))
      (sort-by 
        #(-> % :last :ts))
      (reverse))))
;;

(defn- labeled-value [lbl val]
  (when val
    [:div.value.clearfix
      [:div.col-sm-6.lbl lbl]
      [:div.col-sm-6.val val]]))
;-

(defn format-station [{id :_id title :title last :last trends :trends}]
  (let [t-val   (format-t   nil (:t last) (-> trends :t :avg))
        w-val   (format-w   nil (:w last) (:g last) (:b last))
        p-val   (format-p   nil (:p last))
        h-val   (format-h   nil (:h last))
        wt-val  (format-wt  nil (:wt last) (:wl last))]
    (when (or t-val w-val p-val h-val wt-val)
      [:div.station
        [:div.title (hesc title) " "
          [:a {:href (str ST_BASE_URL id)}
            [:i.fa.fa-bar-chart]
            [:div.hhmm (hhmm (:ts last))]]]
        (labeled-value "температура"      t-val)
        (labeled-value "ветер"            w-val)
        (labeled-value "давление"         p-val)
        (labeled-value "влажность"        h-val)
        (labeled-value "температура воды" wt-val)
        [:div.clearfix]])))
;;

(defn index-brief []
  (let [st-data   (fresh-st-data)]
    [:div.b-meteo-brief
      [:div.col-md-7.col-md-offset-1
        (for [st st-data]
          (format-station st))]
      [:div.col-md-4 {:style "text-align: center"}
        [:div 
          {:style "width: 300px; height: 300px; margin: 12px 8px; display: inline-block; overflow: hidden;"}
          (ya-rtb "R-A-1908-16" true)]]
      [:div.clearfix]]))
;;

(comment
  
  (->> (fresh-st-data)
    (map #(select-keys % [:_id :ts :last])))

  ,)
  
;;.
