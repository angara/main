(ns front.core
  (:require
    [mlib.log :refer [warn]]
    [mlib.conf :refer [conf]]
    [html.frame :refer [render]]))
;

(defn main-page [req]
  (render
    req
    {:topmenu :main
      :page-title "Angara.Net main page"}
    [:.uk-panel.uk-panel-box.uk-panel-space.uk-panel-header
      [:p
        "some text"]

      [:p
        "Главная События Информация Карты Снаряжение Турсервис Объявления Погода Фото Форум"]]

    [:div
      [:hr]
      [:p.txt
        "Активный отдых
Отдых на Байкале Автотранспорт на заказ Предложения турфирм
Альпинизм, скалолазание
Статьи Маршруты Альпфорум
Велосипед
Веломаршруты Соревнования Статьи Магазины Ремонт, запчасти База по угонам Велофорум
Горные лыжи, сноуборд
Магазины Обучение Трассы Ремонт Фрирайд Форум Куплю/Продам
Карты
GPS трэки маршрутов Карты АО ВостСиб АГП
Погода
Иркутск Братск Улан-Удэ река Мамай GIS meteo
Снаряжение
Магазины Прокат, продажа Форум
Рыбалка
Вязка мух Рыболовные места Обсуждение
Спелеология
Пещеры Иркутской области
Статьи
По категориям Индекс по ключевым словам
Прочее
Cпасательные службы Информация Автотранспорт Фото альбомы Заставки на рабочий стол
Объявления
Авто Мото Вело Фото Работа Жильё Услуги Турсервис
Вебсайт Angara.Net
О проекте Авторам Статистика Размещение рекламы"]

      [:p.txt "
Новости

Июн. 22Кино под звездами в спорт-парке Поляна
Ну что, полетели в Спорт-парк ПОЛЯНА в эту пятницу 24 июня на просмотр фильма
На гребне волны?! Сверх люди, восьмерка, преступления и кайф от жизни: об этом фильм.
Погодка обещает быть! Для спортсменов перед началом фильма бонус-ролики ...»»
Антонина Кукушкина
Июн. 19Одиночный забег вокруг Байкала за 50 дней
Дмитрий Ерохин, известный российский ультра-марафонец, планирует пробежать вокруг
озера Байкал маршрут протяжённостью 1800 километров за 50 дней ...»»
Анатолий Казакевич
Июн. 19Мини-путешествие в Монголию
У всех проблем – одно начало. Сидела девушка, скучала :) Поездка была крайне
спонтанна, не организована и сумбурна, Но эмоции от поездки, невероятная красота,
которая открылась моим глазам и масса новых впечатлений – переплюнуло все трудности, и как говорится, лишения ...»»
Дарья Уварова
Июн. 19Соревнования по кабельному вейкборду в Иркутске
Антонина Кукушкина
Июн. 13Второй лыжный поход-марафон: Хужир – Северобайкальск 2016
Александр Абаев
Июн. 1 Под управлением любви  - бардовский концерт на палубе ледокола Ангара
Ирина Русанова
Май. 30Четыре дня в Монголии
Мария Шамбурова
Май. 30Иран 2016. Восхождение на Демавенд, путешествие по городам Персии
Диана Мартынова
Май. 29Чулышман 2016 или непростая майская прогулка. Фоторепортаж
Стас Пономарев
Май. 28Старт экспедиции «Байкал Аляска»
Максим Хвостишков
»» все новости"]

      [:p.txt "
Летающий головастик
Автор:
Протасов Алексей

100 горячих тем ...»»Форум
Ну вот и сбылась мечта идиота (с)
ArtemR27.06.16 09:16Велосипед
Однодневная прогулка: Переезд-Темная Падь 26.06.2016
serebro3827.06.16 09:04Пеший туризм
Ока Саянская
mnv27.06.16 08:59Сплавы
Поездка на Ольхон
petal27.06.16 07:48По Байкалу
Ищу попутчиков до Тёплых озёр на Снежной, Соболиного.
200727.06.16 06:46Ищу попутчика
Практическое управление парусным судном.
max83427.06.16 06:30Под парусом
Тальяны - Торы на велосипеде
direst27.06.16 06:21Велосипед
Bike Fit в Иркутске.
Caesar27.06.16 04:06Велосипед
Привет.На водопады пойдёшь? В выходные озеро Сердце, озеро Утин...
drovosek27.06.16 03:40Пеший туризм
Срочно ищем единомышленников в поход в Тункинские Гольцы (26.07...
Pavell27.06.16 02:31Ищу попутчика
Срочно нужна помощь собаке в АРШАНЕ!
Sordina27.06.16 00:17Домашние питомцы
Отдых в Крыму в октябре
elgrand12327.06.16 00:00Прочее
Кто идет на Шумак в июне
Landg1326.06.16 23:21Горный туризм
Ищу попутчика
Warlock26.06.16 23:07Ищу попутчика
Бухта Песчаная -Б.Голоустное -Листвянка
papako26.06.16 22:46Ищу попутчика
Баргузинский хребет и его почитатели!
kslavina26.06.16 22:30Горный туризм
Приму в дар сломанные звезды и кассеты
Pesegov26.06.16 22:02Велосипед
Веревочный Вуки-парк на Горе Соболиной
tema66626.06.16 20:42Прочее
Сводит ноги
tema66626.06.16 20:13Дайвинг
Трехглавая 25-26 июня
drovosek26.06.16 19:51Горный туризм
100 горячих тем ...»»"]

      [:hr]]))
;

;;.
