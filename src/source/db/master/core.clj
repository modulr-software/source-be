(ns source.db.master.core
  (:require [source.db.master.connection :refer [ds] :as c]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]
            [source.db.master.users :as users]
            [source.db.master.baselines :as baselines]
            [source.db.master.content-type :as content-types]
            [source.db.master.providers :as providers]
            [source.db.master.sectors :as sectors]
            [source.db.master.cadences :as cadences]))

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
    false)
  )

(defn setup-db [ds]
  (when-not (table? ds "users")
    (users/create-users-table ds))
  (when-not (table? ds "cadences")
    (cadences/create-cadences-table ds))
  (when-not (table? ds "content_types")
    (content-types/create-content-types-table ds))
  (when-not (table? ds "providers")
    (providers/create-providers-table ds))
  (when-not (table? ds "baselines")
    (baselines/create-baselines-table ds))
  (when-not (table? ds "sectors")
    (sectors/create-sectors-table ds))
  
  (when-not (records? ds "cadences")
    (seed-table ds {:table "cadences"
                    :cols ["label" "days"]
                    :vals [["daily" 1]
                           ["weekly" 7]
                           ["biweekly" 14]
                           ["monthly" 30]]})
    (seed-table ds {:table "baselines"
                    :cols ["label" "min" "max"]
                    :vals [["0-1000" 0 1000]
                           ["1000-10000" 1000 10000]
                           ["10000-100000" 10000 100000]
                           ["100000-1000000" 100000 1000000]]})
    (seed-table ds {:table "content_types"
                    :cols ["name"]
                    :vals [["video"]
                           ["podcast"]
                           ["blog"]]})
    (seed-table ds {:table "providers"
                    :cols ["name" "domain" "content_type_id"]
                    :vals [["youtube" "www.youtube.com" 1]
                           ["spotify" "www.spotify.com" 2]
                           ["medium" "www.medium.com" 3]]})
    (seed-table ds {:table "sectors"
                    :cols ["name"]
                    :vals [["renewable energy"]
                           ["conservation ecology"]
                           ["recycling"]]})))


(defn drop-tables [ds]
    (loop [tnames (table-names ds)]
      (let [tname (first tnames)]
        (when (some? tname)
          (drop-table ds {:table tname})
          (recur (vec (rest tnames)))))))

(comment
  (setup-db ds)
  (drop-tables ds)
  (drop-table ds {:table "users"})
  (table? ds "users")
  (table ds {:name "bundles"})
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
  (users/users ds)
  )

