(ns source.db.master.core
  (:require [source.db.util :as db.util]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [source.db.master.users :as users]
            [source.db.master.baselines :as baselines]
            [source.db.master.content-type :as content-types]
            [source.db.master.providers :as providers]
            [source.db.master.sectors :as sectors]
            [source.db.master.cadences :as cadences]
            [source.db.master.categories :as categories]
            [source.db.master.feeds-categories :as feeds-categories]
            [source.db.master.outgoing-posts :as oposts]
            [source.db.master.feeds :as feeds]
            [source.db.master.bundles :as bundles]))

(declare table)
(declare tables)
(declare drop-table)
(declare num-records)
(declare seed-table)
(hugsql/def-db-fns "source/db/master/sql/master.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(defn table-names [ds]
  (->> (tables ds)
       (mapv #(:name %))))

(defn table? [ds tname]
  (->
   (table ds {:name tname})
   (some?)))

(defn records? [ds tname]
  (if (table? ds tname)
    (->
     (num-records ds {:table tname})
     (:count)
     (> 0))
    false))

(def seeders
  [{:name "cadences" :create cadences/create-cadences-table :seed true}
   {:name "content_types" :create content-types/create-content-types-table :seed true}
   {:name "providers" :create providers/create-providers-table :seed true}
   {:name "baselines" :create baselines/create-baselines-table :seed true}
   {:name "sectors" :create sectors/create-sectors-table :seed true}
   {:name "categories" :create categories/create-table :seed true}
   {:name "outgoing_posts" :create oposts/create-outgoing-posts-table :seed false}
   {:name "feeds" :create feeds/create-table :seed false}
   {:name "feeds_categories" :create feeds-categories/create-table :seed false}
   {:name "users" :create users/create-users-table :seed false}
   {:name "bundles" :create bundles/create-bundles-table :seed false}])

(defn drop-tables [ds]
  (loop [tnames (table-names ds)]
    (let [tname (first tnames)]
      (when (some? tname)
        (drop-table ds {:table tname})
        (recur (vec (rest tnames)))))))

(defn setup-db [ds seeders]
  (drop-tables ds)
  (doseq [{:keys [name create seed]} seeders]
    (when-not (table? ds name)
      (create ds))
    (when (and seed (not (records? ds name)))
      (->> name
           (keyword)
           (db.util/seed-data)
           (db.util/apply-seed ds name)))))

(defn get-post-categories [ds post-id]
  (let [feed-id (-> (oposts/select-outgoing-post-by-id ds {:id post-id})
                    (:feed_id))]
    (feeds-categories/select-by-feed-id ds {:feed-id feed-id})))

(comment
  (def ds (db.util/conn "master"))
  (setup-db ds seeders)
  (users/insert-user ds {:email "merveillevaneck@gmail.com"
                         :password "test"
                         :firstname "merv"
                         :lastname "ilicious"
                         :business-name "modulr"
                         :type "creator"})
  (bundles/insert-bundle ds {:cols ["user_id"]
                             :vals [1]})
  (feeds/insert ds {:cols ["title"
                           "rss_url"
                           "user_id"
                           "created_at"
                           "content_type_id"
                           "cadence_id"
                           "baseline_id"]
                    :vals ["AFEED"
                           "sdfsf"
                           1
                           "sdfsfsf"
                           1
                           1
                           1]})
  (oposts/insert-outgoing-post ds {:bundle-id 1
                                   :title "A bloody title"
                                   :subtitle "subtitle"
                                   :stream-url "sdfsf"
                                   :content-type "video"
                                   :feed-id 1
                                   :creator-id 1})
  (feeds-categories/insert-feeds-categories ds {:cols ["feed_id"
                                                       "category_id"]
                                                :vals [1
                                                       1]})

  (feeds-categories/insert-feeds-categories ds {:cols ["feed_id"
                                                       "category_id"]
                                                :vals [1
                                                       2]})

  (feeds-categories/insert-feeds-categories ds {:cols ["feed_id"
                                                       "category_id"]
                                                :vals [1
                                                       3]})
  (bundles/select-all-bundles ds)
  (feeds/select-all ds)
  (feeds-categories/select-all ds)
  (oposts/select-outgoing-post-by-id ds {:id 1})
  (get-post-categories ds 1)
  (drop-tables ds)
  (drop-table ds {:table "users"})
  (table? ds "users")
  (table ds {:name "categories"})
  (drop-tables ds)
  (->
   (table-names ds))

  (table? ds "users")
  (table ds {:table "users"})

  (seed-table ds {:table "cadences"
                  :cols ["label" "days"]
                  :vals [["daily" 1]
                         ["weekly" 7]
                         ["biweekly" 14]
                         ["monthly" 30]]})
  (cadences/cadences ds)
  (baselines/baselines ds)
  (sectors/sectors ds)
  (users/users ds))

