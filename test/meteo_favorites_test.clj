(ns meteo-favorites-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer [deftest is run-tests use-fixtures]]
    [jsonista.core :as json]
    [meteo.core :as meteo]
    [meteo.data :as data]
    [meteo.favorites :as favorites]
    [html.frame :as frame]
    [mlib.web.middleware :as middleware]
    [next.jdbc :as next-jdbc]
    [sql.conn :as conn]
    [sql.jdbc :as jdbc]
    [sql.meteo :as db]
    [web.app :as web]))

(defn execute [stmt]
  (with-open [connection (conn/dbc)]
    (jdbc/execute connection stmt)))

(use-fixtures :each
  (fn [test-fn]
    (execute ["truncate users"])
    (execute ["insert into users(uid) values (1), (2)"])
    (test-fn)))

(deftest persistent-and-independent-favorites
  (is (= #{} (db/favorites "1")))
  (is (db/set-favorite! "1" "olha" true))
  (is (db/set-favorite! "1" "olha" true))
  (is (= #{"olha"} (db/favorites "1")))
  (is (= #{} (db/favorites "2")))
  (with-open [connection (conn/dbc)]
    (is (= 1 (:n (jdbc/fetch-one connection
                   ["select jsonb_array_length(meteo_favorites) as n from users where uid=1"])))))
  (is (db/set-favorite! "1" "olha" false))
  (is (db/set-favorite! "1" "olha" false))
  (is (= #{} (db/favorites "1"))))

(deftest concurrent-station-updates
  (let [gate (promise)
        workers (mapv (fn [st] (future @gate (db/set-favorite! "1" st true)))
                      ["olha" "uiii" "olha"])]
    (deliver gate true)
    (doseq [worker workers]
      (is (= true (deref worker 5000 ::timeout))))
    (is (= #{"olha" "uiii"} (db/favorites "1")))))

(deftest stable-server-order
  (let [stations (mapv #(hash-map :st %) ["a" "b" "c" "d"])]
    (is (= ["b" "d" "a" "c"]
           (mapv :st (favorites/favorites-first stations #{"b" "d" "offline"}))))
    (is (= stations (favorites/favorites-first stations #{})))))

(def request
  {:user {:id "1"} :sess {:_csrf "test-token"}
   :headers {"x-csrf-token" "test-token"}
   :params {:st "olha" :favorite true}})

(deftest authorization-validation-and-csrf
  (with-redefs [data/active-stations (constantly [{:st "olha"}])]
    (is (= 401 (:status (favorites/update-favorite (dissoc request :user)))))
    (doseq [req [(dissoc request :headers)
                 (assoc request :headers {"x-csrf-token" "wrong"})
                 (dissoc request :sess)]]
      (is (= 403 (:status (favorites/update-favorite req)))))
    (doseq [params [{:st "olha" :favorite "true"}
                    {:st "olha"} {:st "unknown" :favorite true}
                    {:st ["olha"] :favorite true}]]
      (is (= 400 (:status (favorites/update-favorite (assoc request :params params))))))
    (is (= #{} (db/favorites "1")))
    (is (= 200 (:status (favorites/update-favorite request))))
    (is (= #{"olha"} (db/favorites "1")))
    (with-redefs [data/active-stations (constantly [])]
      (is (= 200 (:status (favorites/update-favorite
                           (assoc-in request [:params :favorite] false)))))
      (is (= #{} (db/favorites "1"))))))

(deftest json-route
  (with-redefs [data/active-stations (constantly [{:st "olha"}])]
    (let [handler (middleware/middleware (web/make-routes))
          response (handler
                     (assoc request :request-method :post :uri "/meteo/favorite"
                       :headers {"content-type" "application/json"
                                 "x-csrf-token" "test-token"}
                       :body (java.io.ByteArrayInputStream.
                               (.getBytes "{\"st\":\"olha\",\"favorite\":true}" "UTF-8"))
                       :params {}))]
      (is (= 200 (:status response)))
      (is (= {"ok" 1 "st" "olha" "favorite" true}
             (json/read-value (:body response))))
      (is (= #{"olha"} (db/favorites "1"))))))

(deftest rendered-user-and-guest-pages
  (let [stations [{:st "a" :title "A" :last {:t 1}}
                  {:st "b" :title "B" :last {:t 2}}]
        page (fn [req]
               (with-redefs [data/active-stations (constantly stations)
                             frame/render-layout (fn [_ opts body] [opts body])]
                 (meteo/index-page req)))
        station-nodes (fn [[_ body]] (-> body (nth 1) (nth 2) (nth 1)))]
    (db/set-favorite! "1" "b" true)
    (let [[opts body :as result] (page request)
          nodes (station-nodes result)]
      (is (= ["/incs/meteo/favorites.js"] (:js opts)))
      (is (= "test-token" (get-in body [1 1 :data-csrf])))
      (is (= ["/meteo/st/b" "/meteo/st/a"]
             (mapv #(get-in % [1 2 1 :href]) nodes)))
      (is (= ["★" "☆"] (mapv #(get-in % [1 1 2 2]) nodes))))
    (let [[opts _ :as result] (page {})
          nodes (station-nodes result)]
      (is (nil? (:js opts)))
      (is (= ["/meteo/st/a" "/meteo/st/b"]
             (mapv #(get-in % [1 2 1 :href]) nodes)))
      (is (every? nil? (map #(get-in % [1 1 2]) nodes))))))

(defn -main []
  ;; Use a private schema so the suite never touches application tables.
  (let [url (or (System/getenv "METEO_TEST_JDBC_URL")
                (throw (ex-info "Set METEO_TEST_JDBC_URL" {})))
        schema (str "meteo_test_" (str/replace (str (random-uuid)) "-" ""))]
    (with-open [connection (next-jdbc/get-connection url)]
      (jdbc/execute connection [(str "create schema " schema)]))
    (try
      (with-redefs [conn/dbc
                    (fn []
                      (let [connection (next-jdbc/get-connection url)]
                        (jdbc/execute connection [(str "set search_path to " schema)])
                        connection))]
        (execute ["create table users (uid integer primary key)"])
        (dotimes [_ 2]
          (execute [(slurp "sql/2026-10-30-meteo-favorites.sql")]))
        (let [result (run-tests 'meteo-favorites-test)]
          (when (pos? (+ (:fail result) (:error result)))
            (throw (ex-info "Meteo favorites tests failed" result)))))
      (finally
        (with-open [connection (next-jdbc/get-connection url)]
          (jdbc/execute connection [(str "drop schema " schema " cascade")]))
        (shutdown-agents)))))
