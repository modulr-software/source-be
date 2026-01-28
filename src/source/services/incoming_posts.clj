(ns source.services.incoming-posts
  (:require [source.db.interface :as db]
            [source.db.honey :as hon]))

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

(defn incoming-posts-with-feeds
  [ds {:keys [_where] :as opts}]
  (hon/execute! ds
                (merge
                 {:select [[:incoming-posts.id :id] :incoming-posts.post-id :feed-id]
                  :from :incoming-posts
                  :join [:feeds [:= :incoming-posts.feed-id :feeds.id]]}
                 opts)
                {:ret :*}))

(defn incoming-post [ds {:keys [id where] :as opts}]
  (->> {:tname :incoming-posts
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(defn categories-by-posts [ds {:keys [_where] :as opts}]
  (hon/execute! ds
                (merge
                 {:select [[:incoming-posts.id :post-id] [:categories.id :id] :categories.name]
                  :from :incoming-posts
                  :join [:feeds [:= :incoming-posts.feed-id :feeds.id]]
                  :left-join [:feed-categories [:= :feed-categories.feed-id :feeds.id]]
                  :right-join [:categories [:= :categories.id :feed-categories.category-id]]}
                 opts)
                {:ret :*}))
