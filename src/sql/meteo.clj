(ns sql.meteo
  (:require
    [jsonista.core :as json]
    [mlib.core :refer [to-int]]
    [sql.conn :refer [dbc]]
    [sql.jdbc :as jdbc]))

(defn favorites [uid]
  (with-open [conn (dbc)]
    (let [row (jdbc/fetch-one conn
                ["select meteo_favorites::text as favorites from users where uid = ?"
                 (to-int uid)])]
      (set (json/read-value (or (:favorites row) "[]"))))))

(defn set-favorite! [uid st favorite]
  ;; Update one station atomically; concurrent tabs must not replace the array.
  (with-open [conn (dbc)]
    (= 1
       (jdbc/execute conn
         (if favorite
           [(str "update users set meteo_favorites = case"
                 " when meteo_favorites @> ?::jsonb then meteo_favorites"
                 " else meteo_favorites || ?::jsonb end where uid = ?")
            (json/write-value-as-string [st])
            (json/write-value-as-string [st]) (to-int uid)]
           ["update users set meteo_favorites = meteo_favorites - cast(? as text) where uid = ?"
            st (to-int uid)])))))
