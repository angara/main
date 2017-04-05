
(ns tourserv.const)


(def TYPES
  [ { :id     "apart"
      :title  "Проживание"}
    { :id     "auto"
      :title  "Автотранспорт"}
    { :id     "equip"
      :title  "Снаряжение на прока"}
    { :id     "guide"
      :title  "Гиды и экскурсоводы"}])
;

(def TYPE_MAP
  (->> TYPES
    (map #(vector (:id %) %))
    (into {})))
;

(def TOWNS
  [
    { :id "arshan"    :title "Аршан"}
    { :id "baik"      :title "Байкальск"}
    { :id "vydrino"   :title "Выдрино"}
    { :id "goloust"   :title "Голоустное"}
    { :id "irkutsk"   :title "Иркутск"}
    { :id "listv"     :title "Листвянка"}
    { :id "smallsea"  :title "Малое Море"}
    { :id "olkhon"    :title "Ольхон"}
    { :id "sevbaik"   :title "Северобайкальск"}
    { :id "chiv"      :title "Чивыркуй"}
    { :id "shumak"    :title "Шумак"}])
;

(def TOWN_MAP
  (->> TOWNS
    (map #(vector (:id %) %))
    (into {})))
;

;;.
