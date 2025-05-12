(ns modulr.analytics
  (:require [next.jdbc :as jdbc]
            [modulr.db :as db]
            [clojure.set :as set]))(def db-spec {:classname   "org.sqlite.JDBC"
                                                 :subprotocol "sqlite"
                                                 :subname     "analytics-test.db"})



;; SHORT HEURISTIC
(defn short-heuristic [bundle-id]
  (let [posts (jdbc/query db-spec
                          ["SELECT * FROM bundle_post_analytics WHERE bundle_id = ?" bundle-id])
        top-creators (->> posts
                          (group-by :post_id)
                          (map (fn [[id posts]] [id (reduce + (map :clicks posts))]))
                          (sort-by second >)
                          (take 3)
                          (map first)
                          set)]
    (sort-by (juxt (comp - :trend)
                   (comp not top-creators :post_id)
                   (comp str :date))
             posts)))

(defn create-bundle-post-analytics-table []
  (jdbc/execute! db/*ds* ["
    CREATE TABLE IF NOT EXISTS bundle_post_analytics (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      bundle_id TEXT,
      post_id TEXT,
      likes INTEGER,
      comments INTEGER,
      clicks INTEGER
    );"]))

(defn create-bundle-aggregate-table []
  (jdbc/execute! db/*ds* ["
    CREATE TABLE IF NOT EXISTS bundle_aggregate (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      bundle_id TEXT,
      avg_clicks REAL,
      avg_likes REAL,
      avg_comments REAL
    );"]))

(defn insert-bundle-post-analytics [data]
  (jdbc/execute! db/*ds*
                 ["INSERT INTO bundle_post_analytics (bundle_id, post_id, likes, comments, clicks)
      VALUES (?, ?, ?, ?, ?)"
                  (:bundle_id data) (:post_id data) (:likes data) (:comments data) (:clicks data)]))

(defn insert-bundle-aggregate [data]
  (jdbc/execute! db/*ds*
                 ["INSERT INTO bundle_aggregate (bundle_id, avg_clicks, avg_likes, avg_comments)
      VALUES (?, ?, ?, ?)"
                  (:bundle_id data) (:avg_clicks data) (:avg_likes data) (:avg_comments data)]))

(defn update-bundle-post-analytics [id updates]
  (jdbc/execute! db/*ds*
                 ["UPDATE bundle_post_analytics SET clicks = ? WHERE id = ?"
                  (:clicks updates) id]))

(comment

  ;; Create necessary tables
  (create-bundle-post-analytics-table)
  (create-bundle-aggregate-table)

  ;; Insert mock data
  (insert-bundle-post-analytics {:bundle_id "test-1"
                                 :post_id "p1"
                                 :likes 10
                                 :comments 5
                                 :clicks 100})

  (insert-bundle-aggregate {:bundle_id "test-1"
                            :avg_clicks 50
                            :avg_likes 12
                            :avg_comments 3})

  ;; Update a row
  (update-bundle-post-analytics 1 {:clicks 999})

  ;; Fetch to see if update worked (just use your own query fn if available)
  (jdbc/execute! db/*ds* ["SELECT * FROM bundle_post_analytics WHERE id = ?" 1])

  ;; Test short-heuristic with fake ID (shouldn't throw)
  (short-heuristic "non-existent-bundle"))
