(ns source.migrations.001-init-master-db
<<<<<<< HEAD
  (:require [clojure.data.json :as json]
            [source.db.master]
            [source.db.honey :as db]
            [source.config :as conf]
            [source.db.tables :as tables]))

(defn read-admins
=======
  (:require [source.db.master.users :as users]
            [source.db.master.bundles :as bundles]
            [source.db.master.baselines :as baselines]
            [source.db.master.cadences :as cadences]
            [source.db.master.feeds :as feeds]
            [source.db.master.feeds-categories :as feeds-categories]
            [source.db.master.providers :as providers]
            [source.db.master.sectors :as sectors]
            [source.db.master.categories :as categories]
            [source.db.master.content-type :as content-types]
            [clojure.data.json :as json]
            [source.db.master.core :as master]
            [source.config :as conf]))

(defn read-admins 
>>>>>>> ba43b5f (Merge main into feat/xml-parse)
  "reads admin user information from file"
  []
  (try
    (-> :admins-path
        (conf/read-value)
        (slurp)
        (json/read-json))
    (catch Exception e
      (println (str "Couldn't read the admins file: " (.getMessage e))))))
<<<<<<< HEAD

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
      :feed-sectors
      :selection-schemas])

    (db/insert! ds-master baselines-seed)
    (db/insert! ds-master cadences-seed)
    (db/insert! ds-master content-types-seed)
    (db/insert! ds-master providers-seed)
    (db/insert! ds-master sectors-seed)

    (run!
     #(db/insert! ds-master {:tname :users
                             :data {:email (:email %)
                                    :password (:password %)
                                    :type "admin"}})
     (read-admins))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (tables/drop-all-tables! ds-master)))

(comment
  (read-admins)
  ())
=======

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (users/create-users-table ds-master)
    (bundles/create-bundles-table ds-master)
    (baselines/create-baselines-table ds-master)
    (cadences/create-cadences-table ds-master)
    (feeds/create-table! ds-master)
    (feeds-categories/create-table! ds-master)
    (providers/create-providers-table ds-master)
    (sectors/create-sectors-table ds-master)
    (categories/create-table ds-master)
    (content-types/create-content-types-table ds-master)
    (master/seed-table ds-master {:table "baselines"
                                  :cols ["label" "min" "max"]
                                  :vals [["0-1000" 0 1000]
                                         ["1000-10000" 1000 10000]
                                         ["10000-100000" 10000 100000]
                                         ["100000-1000000" 100000 1000000]]})
    (master/seed-table ds-master {:table "cadences"
                                  :cols ["label" "days"]
                                  :vals [["daily" 1]
                                         ["weekly" 7]
                                         ["biweekly" 14]
                                         ["monthly" 30]]})
    (master/seed-table ds-master {:table "content_types"
                                  :cols ["name"]
                                  :vals [["video"]
                                         ["podcast"]
                                         ["blog"]]})
    (master/seed-table ds-master {:table "providers"
                                  :cols ["name" "domain" "content_type_id"]
                                  :vals [["youtube" "www.youtube.com" 1]
                                         ["spotify" "www.spotify.com" 2]
                                         ["medium" "www.medium.com" 3]]})
    (master/seed-table ds-master {:table "sectors"
                                  :cols ["name"]
                                  :vals [["renewable energy"]
                                         ["conservation ecology"]
                                         ["recycling"]]})

    (doseq [{:keys [email password]} (read-admins)]
      (master/seed-table ds-master {:table "users"
                                    :cols ["email" "password" "type"]
                                    :vals [[email password "admin"]]}))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (master/drop-tables ds-master)))

(comment 
  (read-admins))
>>>>>>> ba43b5f (Merge main into feat/xml-parse)
