(ns source.workers.bundles
  (:require [source.db.util :as db.util]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]
            [clojure.set :as set]
            [source.services.feed-categories :as feed-categories]))

(defn get-bundle-categories
  "Get all categories for feeds/posts in bundle"
  [ds bundle-id]
  (let [bundle-ds (db.util/conn :bundle bundle-id)
        feed-ids (->> (hon/find bundle-ds {:tname :outgoing-posts
                                           :ret :*})
                      (mapv :feed-id))
        category-ids (->> (hon/find ds {:tname :feed-categories
                                        :where [:in :feed-id feed-ids]
                                        :ret :*})
                          (mapv :category-id))]
    (hon/find ds {:tname :categories
                  :where [:in :id category-ids]
                  :ret :*})))

(defn get-outgoing-feeds
  "Gets a filtered list of outgoing feeds for the associated bundle."
  [ds {:keys [bundle-id type latest category-ids nonfiltered]}]
  (let [bundle-ds (db.util/conn :bundle bundle-id)
        feed-ids (mapv :feed-id (hon/find bundle-ds {:tname :outgoing-posts
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
        query (-> (hsql/where (when type [:= :content-type-id type])
                              [:in :id category-filtered-feed-ids]
                              (when (seq blocked-feed-ids) [:not [:in :id blocked-feed-ids]]))
                  (assoc :order-by (when latest [[:created-at :desc]]))
                  (merge {:tname :feeds
                          :ret :*}))
        type-filtered (hon/find ds query)]
    type-filtered))

(defn get-outgoing-posts
  "Get outgoing posts based on short heuristics and update analytics impressions"
  [ds {:keys [bundle-id limit start type latest category-ids]}]
  (let [bundle-ds (db.util/conn :bundle bundle-id)
        all-feed-ids (mapv :id (hon/find ds {:tname :feeds
                                             :ret :*}))
        blocked-feed-ids (mapv :feed-id (hon/find ds {:tname :filtered-feeds
                                                      :where [:= :bundle-id bundle-id]
                                                      :ret :*}))
        available-feed-ids (vec (remove (set blocked-feed-ids) all-feed-ids))

        blocked-post-ids (mapv :post-id (hon/find ds {:tname :filtered-posts
                                                      :where [:= :bundle-id bundle-id]
                                                      :ret :*}))

        filtered-posts (hon/find bundle-ds (-> (hsql/where (when type [:= :content-type-id type])
                                                           (when (seq blocked-post-ids) [:not [:in :id blocked-post-ids]])
                                                           [:in :feed-id available-feed-ids])
                                               (assoc :order-by (when (= latest "true") [[[:posted-at :desc]]]))
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

        valid-limit? (and (some? limit) (> (count started-posts) limit))
        limited-posts (if valid-limit?
                        (subvec started-posts 0 limit)
                        started-posts)]
    limited-posts))
