(ns source.migrations.001-init-master-db
  (:require [source.admins :as admins]
            [source.db.master]
            [source.db.honey :as db]
            [source.db.tables :as tables]
            [source.config :as conf]))

(def baselines-seed
  {:tname :baselines
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

(def cadences-seed
  {:tname :cadences
   :data [{:label "daily"
           :days 1}
          {:label "weekly"
           :days 7}
          {:label "biweekly"
           :days 14}
          {:label "monthly"
           :days 30}]})

(def content-types-seed
  {:tname :content-types
   :data [{:name "video"}
          {:name "podcast"}
          {:name "blog"}]})

(def providers-seed
  {:tname :providers
   :data [{:name "youtube"
           :domain "www.youtube.com"
           :content-type-id 1}
          {:name "spotify"
           :domain "www.spotify.com"
           :content-type-id 2}
          {:name "medium"
           :domain "www.medium.com"
           :content-type-id 3}]})

(def sectors-seed
  {:tname :sectors
   :data [{:name "renewable energy"}
          {:name "conservation ecology"}
          {:name "recycling"}]})

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
      :feed-categories
      :providers
      :businesses
      :user-sectors
      :feed-sectors])

    (db/insert! ds-master baselines-seed)
    (db/insert! ds-master cadences-seed)
    (db/insert! ds-master content-types-seed)
    (db/insert! ds-master providers-seed)
    (db/insert! ds-master sectors-seed)

    (when (= (conf/read-value :env) "dev")
      (admins/encrypt!))

    (let [admins (admins/read)]
      (when (not (empty? admins))
        (run!
         #(db/insert! ds-master {:tname :users
                                 :data {:email (:email %)
                                        :password (:password %)
                                        :type "admin"}})
         admins)))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (tables/drop-all-tables! ds-master)))

(comment
  (admins/read)
  ())
