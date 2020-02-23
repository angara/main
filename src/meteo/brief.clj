(ns meteo.brief
  (:require
    ; [clojure.string :as s]
    [clj-time.core :as tc]
    ; 
    [mlib.config        :refer [conf]]
    [mlib.core          :refer [hesc]]
    [mlib.time          :refer [hhmm]]
    [mlib.web.snippets  :refer [ya-rtb]]
    ; ;
    [meteo.db           :refer [st-ids]]
    [meteo.fmt          :refer [format-t format-h format-p format-w format-wt]]))
;=


(def ST_DEAD_INTERVAL (tc/days 7))   ;; duplicated in index
(def ST_BASE_URL      "/meteo/st/")  ;; duplicated in index

(defn fresh-st-data []
  (let [st-list (get-in conf [:main :meteo :st_default])
        t0  (tc/minus (tc/now) ST_DEAD_INTERVAL)]
    (->>
      (st-ids st-list)
      (keep
        #(when-let [ts (get-in % [:last :ts])]
          (when (tc/after? ts t0)
            %)))
      (sort-by 
        #(get-in % [:last :ts]))
      (reverse))))
;;

(defn format-station [{id :_id title :title last :last trends :trends}]
  (let [vals  (remove nil? 
                [ (format-t "<span class=\"lbl\">температура</span>"  (:t last) (get-in trends [:t :avg]))
                  (format-w "<span class=\"lbl\">ветер</span>"        (:w last) (:g last) (:b last))
                  (format-p "<span class=\"lbl\">давление</span>"     (:p last))
                  (format-h "<span class=\"lbl\">влажность</span>"    (:h last))
                  (format-wt  "<span class=\"lbl\">температура воды</span>" (:wt last) (:wl last))])]
    (when (seq vals)
      [:div.station
        [:div.title (hesc title) " "
          [:a {:href (str ST_BASE_URL id)}
            [:i.fa.fa-bar-chart]
            [:div.hhmm (hhmm (:ts last))]]]
        (for [v vals]
          [:div.value v])])))
;;

(defn index-brief []
  (let [st-data   (fresh-st-data)]
    [:div.b-meteo-brief
      [:div.col-md-7.col-md-offset-1
        (for [st st-data]
          (format-station st))]
      [:div.col-md-4 {:style "text-align: center"}
        [:div 
          {:style "width: 300px; height: 300px; margin: 12px 8px; display: inline-block;"}
          (ya-rtb "R-A-1908-16" nil)]]
      [:div.clearfix]]))
;;

(comment
  
  (->> (fresh-st-data)
    (map #(select-keys % [:_id :ts :last])))

  ,)
  
;;.
