(ns source.workers.bundles
  (:require [source.db.util :as db.util]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]
            [source.services.analytics.interface :as analytics]
            [clojure.set :as set]
            [source.services.feed-categories :as feed-categories]))

(defn get-bundle-categories [ds bundle-id]
  (with-open [bundle-ds (db.util/conn :bundle bundle-id)]
    (let [feed-ids (->> (hon/find bundle-ds {:tname :outgoing-posts
                                             :ret :*})
                        (mapv :feed-id))
          category-ids (->> (hon/find ds {:tname :feed-categories
                                          :where [:in :feed-id feed-ids]
                                          :ret :*})
                            (mapv :category-id))]
      (hon/find ds {:tname :categories
                    :where [:in :id category-ids]
                    :ret :*}))))

(defn get-feeds-in-bundle!
  "Gets a filtered list of feeds from the bundle. Updates analytics impressions."
  [ds {:keys [bundle-id type latest category-ids nonfiltered]}]
  (with-open [bundle-ds (db.util/conn :bundle bundle-id)]
    (let [feed-ids (mapv :feed-id (hon/find bundle-ds {:tname :outgoing-posts
                                                       :ret :*}))
          category-filtered-feed-ids (if (empty? category-ids)
                                       feed-ids
                                       (->> (hsql/where
                                             [:in :feed-id feed-ids]
                                             [:in :category-id category-ids])
                                            (merge {:tname :feed-categories
                                                    :ret :*})
                                            (hon/find ds)
                                            (mapv :feed-id)))
          blocked-feed-ids (if (some? nonfiltered)
                             []
                             (mapv :feed-id (hon/find ds {:tname :filtered-feeds
                                                          :where [:= :bundle-id bundle-id]
                                                          :ret :*})))
          query (-> (when type [:= :content-type-id type])
                    (hsql/where [:in :id category-filtered-feed-ids]
                                [:not [:in :id blocked-feed-ids]])
                    (hsql/order-by (when latest [:created-at :desc]))
                    (merge {:tname :feeds
                            :ret :*}))
          type-filtered (hon/find ds query)]

      (analytics/insert-feed-impressions! ds type-filtered bundle-id)
      type-filtered)))

(defn get-feed-in-bundle! [ds {:keys [bundle-id feed-id]}]
  (let [feed (hon/find-one ds {:tname :feeds
                               :where [:= :id feed-id]})]
    (analytics/insert-feed-click! ds feed bundle-id)
    feed))

(defn get-posts-by-feed-in-bundle! [ds {:keys [bundle-id feed-id]}]
  (let [posts (hon/find ds {:tname :incoming-posts
                            :where [:= :feed-id feed-id]
                            :ret :*})]
    (analytics/insert-post-impressions! ds posts bundle-id)
    posts))

(defn get-post-by-feed-in-bundle! [ds {:keys [bundle-id post-id]}]
  (let [post (hon/find-one ds {:tname :incoming-posts
                               :where [:= :id post-id]})]
    (analytics/insert-post-click! ds post bundle-id)
    post))

(defn get-outgoing-posts! [ds {:keys [bundle-id limit start type latest category-ids]}]
  (with-open [bundle-ds (db.util/conn :bundle bundle-id)]
    (let [content-type-comp (when type [:= :content-type-id type])
          start (when start (try (Integer/parseInt start) (catch Exception _)))
          limit (when limit (try (Integer/parseInt limit) (catch Exception _)))

          all-feed-ids (mapv :id (hon/find ds {:tname :feeds
                                               :ret :*}))
          blocked-feed-ids (mapv :feed-id (hon/find ds {:tname :filtered-feeds
                                                        :where [:= :bundle-id bundle-id]
                                                        :ret :*}))
          available-feed-ids (vec (remove (set blocked-feed-ids) all-feed-ids))

          blocked-post-ids (mapv :post-id (hon/find ds {:tname :filtered-posts
                                                        :where [:= :bundle-id bundle-id]
                                                        :ret :*}))

          filtered-posts (hon/find bundle-ds (-> (hsql/where content-type-comp
                                                             [:not [:in :id blocked-post-ids]]
                                                             [:in :feed-id available-feed-ids])
                                                 (hsql/order-by (when (= latest "true") [[:posted-at :desc]]))
                                                 (merge {:tname :outgoing-posts
                                                         :ret :*})))

          categorised-posts (vec
                             (if (seq category-ids)
                               (->> filtered-posts
                                    (mapv
                                     (fn [post]
                                       (when (seq (set/intersection
                                                   (set category-ids)
                                                   (->> {:feed-id (:feed-id post)}
                                                        (feed-categories/categories-by-feed ds)
                                                        (mapv :id)
                                                        (set))))
                                         post)))
                                    (remove nil?))
                               filtered-posts))

          valid-start? (and (some? start) (>= start 0) (< start (count categorised-posts)))
          started-posts (if valid-start?
                          (subvec categorised-posts start)
                          categorised-posts)

          limited-posts (if (and (some? limit) (> (count started-posts) limit))
                          (subvec started-posts 0 limit)
                          started-posts)]

      (analytics/insert-post-impressions! ds limited-posts bundle-id)
      limited-posts)))

(defn get-outgoing-post! [ds {:keys [bundle-id post-id]}]
  (with-open [bundle-ds (db.util/conn :bundle bundle-id)]
    (let [post (hon/find-one bundle-ds {:tname :outgoing-posts
                                        :where [:= :id post-id]
                                        :ret :1})]
      (analytics/insert-post-click! ds post bundle-id)
      post)))
