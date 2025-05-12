(ns modulr.analytics
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.set :as set]
            [clojure.pprint :refer [pprint]]))


(def db-spec {:classname   "org.sqlite.JDBC"
              :subprotocol "sqlite"
              :subname     "analytics-test.db"})

;; TABLE CREATION
(defn create-bundle_post_analytics-table []
  (jdbc/execute! db-spec
                 ["CREATE TABLE IF NOT EXISTS bundle_post_analytics (
        id INTEGER PRIMARY KEY,
        bundle_id TEXT,
        clicks INTEGER,
        post_id TEXT,
        date DATE,
        trend INTEGER,
        category TEXT,
        completion_percentage REAL,
        completions INTEGER,
        likes INTEGER,
        shared INTEGER,
        comments INTEGER,
        saves INTEGER
    )"]))

(defn create-bundle-aggregate-table []
  (jdbc/execute! db-spec
                 ["CREATE TABLE IF NOT EXISTS bundle_analytics (
        id INTEGER PRIMARY KEY,
        bundle_id TEXT,
        date DATE,
        clicks INTEGER,
        impressions INTEGER,
        views INTEGER,
        influencer1 TEXT,
        influencer2 TEXT,
        influencer3 TEXT
    )"]))

;; HELPERS
(defn today []
  (str (java.time.LocalDate/now)))

;; INSERTION FUNCTIONS
(defn insert-bundle-post-analytics [record]
  (jdbc/insert! db-spec :bundle_post_analytics record))

(defn insert-bundle-aggrigate [record]
  (jdbc/insert! db-spec :bundle_analytics record))

;; UPDATE FUNCTIONS
(defn update-bundle-post-analytics [id record]
  (jdbc/update! db-spec :bundle_post_analytics record ["id = ?" id]))

(defn update-bundle-aggregate [id record]
  (jdbc/update! db-spec :bundle_analytics record ["id = ?" id]))

;; DELETE FUNCTIONS
(defn delete-bundle-post-analytics [id]
  (jdbc/delete! db-spec :bundle_post_analytics ["id = ?" id]))

(defn delete-bundle-aggregate [id]
  (jdbc/delete! db-spec :bundle_analytics ["id = ?" id]))

;; AGGREGATION AND UPDATE FUNCTION
(defn update-aggregate-from-posts [bundle-id date]
  (let [{:keys [clicks completions]} (first (jdbc/query db-spec
                                                        ["SELECT SUM(clicks) AS clicks, SUM(completions) AS completions 
                             FROM bundle_post_analytics 
                             WHERE bundle_id = ? AND date = ?"
                                                         bundle-id date]))
        impressions (+ (* 2 completions) clicks)]
    (jdbc/execute! db-spec
                   ["UPDATE bundle_analytics 
                     SET clicks = ?, views = ?, impressions = ? 
                     WHERE bundle_id = ? AND date = ?"
                    clicks completions impressions bundle-id date])))

;; DATA GENERATORS
(defn rand-post-analytics []
  (let [clicks (+ 50 (rand-int 200))
        completions (+ 10 (rand-int 100))
        impressions (+ (* 2 completions) clicks)]
    {:bundle_id (str "bundle-" (rand-int 5))
     :clicks clicks
     :post_id (str (java.util.UUID/randomUUID))
     :date (today)
     :trend (int (* 100 (/ clicks (max 1 impressions))))
     :category (rand-nth ["news" "sports" "entertainment" "tech" "lifestyle"])
     :completion_percentage (* 100 (rand))
     :completions completions
     :likes (rand-int 100)
     :shared (rand-int 50)
     :comments (rand-int 30)
     :saves (rand-int 20)}))

(defn rand-aggrigate []
  {:bundle_id (str "bundle-" (rand-int 5))
   :date (today)
   :clicks 0
   :impressions 0
   :views 0
   :influencer1 nil
   :influencer2 nil
   :influencer3 nil})

;; POPULATE FUNCTIONS
(defn populate-bundle-post-analytics [n]
  (jdbc/with-db-transaction [t-con db-spec]
    (run! #(jdbc/insert! t-con :bundle_post_analytics %)
          (repeatedly n rand-post-analytics))))

(defn populate-bundle-aggrigate [n]
  (jdbc/with-db-transaction [t-con db-spec]
    (run! #(jdbc/insert! t-con :bundle_analytics %)
          (repeatedly n rand-aggrigate))))

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

;; USAGE EXAMPLES
(comment
  ;; Create tables
  (create-bundle_post_analytics-table)
  (create-bundle-aggregate-table)

  ;; Populate with dummy data
  (populate-bundle-post-analytics 100)
  (populate-bundle-aggrigate 10)

  ;; Insert example
  (insert-bundle-post-analytics {:bundle_id "bundle-1"
                                 :clicks 100
                                 :post_id "post-abc"
                                 :date (today)
                                 :trend 3
                                 :category "news"
                                 :completion_percentage 75.5
                                 :completions 80
                                 :likes 45
                                 :shared 20
                                 :comments 15
                                 :saves 10})

  (insert-bundle-aggrigate {:bundle_id "bundle-1"
                            :date (today)
                            :clicks 0
                            :impressions 0
                            :views 0
                            :influencer1 nil
                            :influencer2 nil
                            :influencer3 nil})

  ;; Update example
  (update-bundle-post-analytics 1 {:likes 60})
  (update-bundle-aggregate 1 {:clicks 200 :impressions 500})

  ;; Delete example
  (delete-bundle-post-analytics 1)
  (delete-bundle-aggregate 1)

  ;; Aggregate update
  (update-aggregate-from-posts "bundle-1" (today)))
