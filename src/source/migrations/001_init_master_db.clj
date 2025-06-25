(ns source.migrations.001-init-master-db
  (:require [clojure.data.json :as json]
            [source.db.master.core :as master]
            [source.db.master]
            [source.db.honey :as db]
            [source.db.master :as master-test]
            [source.config :as conf]
            [source.db.tables :as tables]))

(defn read-admins
  "reads admin user information from file"
  []
  (try
    (-> :admins-path
        (conf/read-value)
        (slurp)
        (json/read-json))
    (catch Exception e
      (println (str "Couldn't read the admins file: " (.getMessage e))))))

(defn run-up! [context]
  (let [ds-master (:db-master context)]

    (tables/create-tables!
     ds-master
     :source.db.master
     [:users
      :sectors
      :categories
      :content-types
      :cadences
      :baselines
      :bundles
      :feeds
      :feeds-categories
      :providers])

    (db/insert! ds-master {:tname :baselines
                           :data [{:label "0-1000"
                                   :min 0
                                   :max 1000}
                                  {:label "1000-10000"
                                   :min 1000
                                   :max 10000}
                                  {:label "10000-100000"
                                   :min 10000
                                   :max 100000}
                                  {:label "100000-1000000"
                                   :min 100000
                                   :max 1000000}]})

    (db/insert! ds-master {:tname :cadences
                           :data [{:label "daily"
                                   :days 1}
                                  {:label "weekly"
                                   :days 7}
                                  {:label "biweekly"
                                   :days 14}
                                  {:label "monthly"
                                   :days 30}]})

    (db/insert! ds-master {:tname :content-types
                           :data [{:name "video"}
                                  {:name "podcast"}
                                  {:name "blog"}]})

    (db/insert! ds-master {:tname :providers
                           :data [{:name "youtube"
                                   :domain "www.youtube.com"
                                   :content-type-id 1}
                                  {:name "spotify"
                                   :domain "www.spotify.com"
                                   :content-type-id 2}
                                  {:name "medium"
                                   :domain "www.medium.com"
                                   :content-type-id 3}]})

    (db/insert! ds-master {:tname :sectors
                           :data [{:name "renewable energy"}
                                  {:name "conservation ecology"}
                                  {:name "recycling"}]})

    (doseq [{:keys [email password]} (read-admins)]
      (db/insert! ds-master {:tname :users
                             :data {:email email
                                    :password password
                                    :type "admin"}}))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (tables/drop-all-tables! ds-master)))

(comment
  (read-admins))
