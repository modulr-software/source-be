(ns source.services.incoming-posts
  (:require [source.db.interface :as db]))

(defn insert-incoming-post! [ds {:keys [data ret] :as opts}]
  (->> {:tname :incoming-posts
        :data data
        :ret ret}
       (merge opts)
       (db/insert! ds)))

(defn update-incoming-post! [ds {:keys [id data where] :as opts}]
  (->> {:tname :incoming-posts
        :data data
        :where (if (some? id) [:= :id id] where)
        :ret :1}
       (merge opts)
       (db/update! ds)))

(defn incoming-posts
  ([ds] (incoming-posts ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :incoming-posts
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn incoming-post [ds {:keys [id where] :as opts}]
  (->> {:tname :incoming-posts
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
