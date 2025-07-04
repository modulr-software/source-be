(ns source.services.bundles
  (:require [source.db.interface :as db]))

(defn bundle [ds {:keys [id where] :as opts}]
  (->> {:tname :bundles
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(defn insert-bundle! [ds {:keys [_values _ret] :as opts}]
  (->> {:tname :bundles}
       (merge opts)
       (db/insert! ds)))

(comment 
  (require '[source.db.util :as db.util])
  (db/find (db.util/conn :master) {:tname ""})
  ())
