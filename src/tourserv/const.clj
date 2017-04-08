
(ns tourserv.const)


(def TYPES
  [
    { :id     "auto"
      :title  "Автотранспорт"
      :descr
        (str
          "<ul>"
          "<li>Заброски тургрупп</li>"
          "<li>Доставка в труднодоступные районы</li>"
          "<li>Микроавтобусы в Листвянку, на Малое Море, на Ольхон,"
          " в Монды, в Аршан, в Орлик</li>"
          "<li>Легковой транспорт</li>"
          "</ul>")}
    { :id     "apart"
      :title  "Проживание"
      :descr
        (str
          "<ul>"
          "<li>Турбазы, кэмпинги</li>"
          "<li>Дома для туристов</li>"
          "<li>Квартиры в наём</li>"
          "<li>Размещение: Аршан, Байкальск, Листвянка, Малое Море,"
          " Ольхон, Шумак и в других местах Прибайкалья</li>"
          "</ul>")}
    { :id     "equip"
      :title  "Снаряжение на прокат"
      :descr
        (str
          "<ul>"
          "<li>Палатки, спальники, рюкзаки</li>"
          "<li>Костровое оборудование, горелки</li>"
          "<li>Радиостанции, бинокли</li>"
          "<li>Надувные лодки, байдарки, спасжилеты</li>"
          "<li>Ледорубы, кошки, трекинговые палки</li>"
          "</ul>")}
    { :id     "guide"
      :title  "Гиды и экскурсоводы"
      :descr
        (str
          "<ul>"
          "<li>Походы по Саянам</li>"
          "<li>Сплавы по горным рекам</li>"
          "<li>Велопутешествия по Ольхону и Тажеранам</li>"
          "<li>Экскурсии по Иркутску и КБЖД</li>"
          "<li>Консультации</li>"
          "</ul>")}])
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
