(ns source.services.bundles
  (:require [source.db.interface :as db]))

(defn insert-bundle! [ds {:keys [_values _ret] :as opts}]
  (->> {:tname :bundles}
       (merge opts)
       (db/insert! ds)))

(defn update-bundle! [ds {:keys [id data where] :as opts}]
  (->> {:tname :bundles
        :values data
        :where (if (some? id) [:= :id id] where)
        :ret :1}
       (merge opts)
       (db/update! ds)))

(defn bundles
  ([ds] (bundles ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :bundles
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn bundle [ds {:keys [id where] :as opts}]
  (->> {:tname :bundles
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
