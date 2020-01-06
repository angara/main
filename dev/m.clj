(ns m
  (:import
    [java.util Date]
    [org.bson.types ObjectId])
  (:require
    [honeysql.core :as sql]
    [honeysql.helpers :as h]
    [mount.core :refer [defstate]]
    [monger.collection :as mc]
    [monger.query :as mq]
    ;
    [clj-time.coerce :as tc]
    ;
    ; [mlib.config :refer [conf]]
    ;
    [mdb.core :refer [connect]]
    [sql.core :refer [fetch]]))
;=

(def MDB {:url "mongodb://meteo:metaweb.19@dbm:4417/meteo"})

(defstate meteo-db
  :start
    (connect MDB))

(def LOC_ST 
  { 1 "icc"
    2 "sokr"
    3 "uiii"
    4 "poml"
    5 "uibb"
    6 "uiuu"
    7 "amik"
    8 "slud"
    9 "iood"
   10 "irgp"
   11 "mamai"
   12 "lerm297"
   14 "sobol"
   15 "meget"})
;=
  
(def LOC_TYP
  { "T" :t
    "P" :p
    "H" :h
    "W" :w
    "G" :g})
;=


(defn find-dat [query]
  (-> (:db meteo-db)
    (mq/with-collection "dat"
      (mq/find query)
      (mq/sort {:ts 1})
      (mq/limit 1))))
;;

(defn locs []
  (->
    (h/select :*)
    (h/from :loc)
    ;(h/where [:= :tid (to-int tid 0)])
    (fetch)))
    ;(first)))
;=

(defn dats [limit]
  (->
    (h/select :*)
    (h/from :dat)
    (h/where 
      ; [:and 
      ;   [:= :loc_id 12]
        [:< :stamp (sql/raw "'2013-02-17'::timestamp")])    
    (h/order-by [:stamp])
    (h/limit limit)
    (fetch)))
;=

(defn sql->mng [d]
  (when-let [st (get LOC_ST (:loc_id d))]
    (when-let [fld (get LOC_TYP (:typ d))]
      (let [ts    (tc/to-date (:stamp d))
            data  {fld (:val d)}
            data  (if (#{:w :g} fld)
                    (assoc data :b (:ext d))
                    data)]
        (merge data
          { :_id
            (ObjectId. 
              ^Date   ts
              (int    (:loc_id d)) 
              (short  0) 
              (int    0))
            :st st
            :ts ts})))))
;;

(defn upsert [d]
  (-> (:db meteo-db)
    (mc/update :dat1
      {:_id (:_id d)}
      {:$set (dissoc d :_id)}
      {:upsert true})
    (.getN)
    (= 1)))
;;
      
(comment

  (find-dat {:st "olha"})

  (ObjectId. (Date.) (int 1) (short 2) (int 0))
 
  (locs)

  (doseq [t (dats 10000000)]
    (when-let [d (sql->mng t)]
      ;(prn d 
        (upsert d)))

      
    ; (prn (tc/to-date (:stamp t))) 
    ; (prn 
    ;   t
    ;   (ObjectId. 
    ;     ^Date (tc/to-date (:stamp t)) 
    ;     (int    (:loc_id t)) 
    ;     (short  0) 
    ;     (int    0))))

  (sql/format 
    (->
      (h/select :*)
      (h/from :dat)
      (h/where 
        [:and
          [:= :typ 'T']
          [:< :stamp (sql/raw "'2013-01-19'::timestamp")]])
      (h/order-by [:stamp])
      (h/limit 5)))

  (sql/raw "'2013-02-17'::timestamp")

  ,)


;;.



;  14 | sobol   | 51.510000 | 104.120600 |  535 | Гора Соболиная            | Гора Соболиная, г.Байкальск           |   0
;  15 | meget   | 52.418800 | 104.056900 |  430 | Мегет                     | поселок Мегет                         |   0
;   5 | uibb    | 56.364483 | 101.716063 |  484 | а/п Братск                | Братский Аэропорт                     | -55
;   3 | uiii    | 52.267288 | 104.366972 |  495 | Аэропорт                  | Иркутский Аэропорт                    | -54
;   6 | uiuu    | 51.807384 | 107.445668 |  507 | а/п Улан-Удэ              | Аэропорт Улан-Удэ, Мухино             | -53
;   1 | icc     | 52.240758 | 104.273762 |  457 | ИрВЦ                      | ИДСТУ СО РАН, Лермонтова 134          |   0
;   7 | amik    | 52.316406 | 104.238084 |  430 | Кэш & Керри, Трактовая 18 | Кэш & Керри, Трактовая 18             |   0
;   8 | slud    | 51.656496 | 103.717292 |  470 | Слюдянка                  | Городская Администрация, г.Слюдянка   |   0
;   2 | sokr    | 52.287335 | 104.303332 |  436 | Волконского               | ОБ Сократ, Волконского 2              |   0
;   9 | iood    | 52.309362 | 104.310472 |  482 | Шевцова                   | Областной Онкодиспансер, г.Иркутск    |   0
;  10 | irgp    | 52.283300 | 104.278200 |  440 | Гражданпроект             | Степана Разина, 27, г.Иркутск         |   0
;  11 | mamai   | 51.434000 | 104.797000 |  525 | Мамай                     | река Мамай, Хамар-Дабан               |   0
;  12 | lerm297 | 52.245000 | 104.278000 |  460 | Академгородок             | Лермонтова 297, г.Иркутск             |   0
;   4 | poml    | 52.237032 | 104.274425 |  477 | Помяловского              | Помяловского, 1Б, г.Иркутск           |   0
