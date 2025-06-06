(ns source.db.master.core
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [source.db.master.users :as users]
            [source.db.master.baselines :as baselines]
            [source.db.master.content-type :as content-types]
            [source.db.master.providers :as providers]
            [source.db.master.sectors :as sectors]
            [source.db.master.cadences :as cadences]
            [source.db.master.categories :as categories]
            [source.db.master.feeds-categories :as feeds-categories]
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

(defn drop-tables [ds]
  (loop [tnames (table-names ds)]
    (let [tname (first tnames)]
      (when (some? tname)
        (drop-table ds {:table tname})
        (recur (vec (rest tnames)))))))

(comment
  (def ds (db.util/conn "master"))
  ;; (setup-db ds {:table-names ["cadences"
  ;;                             "baselines"]
  ;;               :setup-data {:cadences {}
  ;;                            :baselines {:seed-data "resources/baselines.json"}}})
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
  (baselines/baselines ds)
  (feeds/select-all ds)
  (feeds-categories/select-all ds)
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

