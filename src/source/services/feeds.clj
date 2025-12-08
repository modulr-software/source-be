(ns source.services.feeds
  (:require [source.db.interface :as db]))

(defn insert-feed! [ds {:keys [data] :as opts}]
  (->> {:tname :feeds
        :data data
        :ret :1}
       (merge opts)
       (db/insert! ds)))

(defn update-feed! [ds {:keys [id data where] :as opts}]
  (->> {:tname :feeds
        :values data
        :where (if (some? id) [:= :id id] where)
        :ret :1}
       (merge opts)
       (db/update! ds)))

(defn feeds
  ([ds] (feeds ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :feeds
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn feed [ds {:keys [id where] :as opts}]
  (->> {:tname :feeds
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(defn delete-feed! [ds {:keys [id where] :as opts}]
  (->> {:tname :feeds
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))
