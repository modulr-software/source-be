(ns source.services.filtered-feeds
  (:require [source.db.interface :as db]))

(defn insert-filtered-feeds! [ds {:keys [data ret] :as opts}]
  (->> {:tname :filtered-feeds
        :data data
        :ret ret}
       (merge opts)
       (db/insert! ds)))

(defn update-filtered-feeds! [ds {:keys [id data where] :as opts}]
  (->> {:tname :filtered-feeds
        :values data
        :where (if (some? id) [:= :id id] where)
        :ret :1}
       (merge opts)
       (db/update! ds)))

(defn filtered-feeds
  ([ds] (filtered-feeds ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :filtered-feeds
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn delete-filtered-feed! [ds {:keys [id where] :as opts}]
  (->> {:tname :filtered-feeds
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))
