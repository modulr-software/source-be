(ns source.services.filtered-posts
  (:require [source.db.interface :as db]))

(defn insert-filtered-posts! [ds {:keys [data ret] :as opts}]
  (->> {:tname :filtered-posts
        :data data
        :ret ret}
       (merge opts)
       (db/insert! ds)))

(defn update-filtered-posts! [ds {:keys [id data where] :as opts}]
  (->> {:tname :filtered-posts
        :values data
        :where (if (some? id) [:= :id id] where)
        :ret :1}
       (merge opts)
       (db/update! ds)))

(defn filtered-posts
  ([ds] (filtered-posts ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :filtered-posts
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn delete-filtered-post! [ds {:keys [id where] :as opts}]
  (->> {:tname :filtered-posts
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))
