(ns source.db.master.core
  (:require [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

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
