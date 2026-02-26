(ns source.workers.bundles
  (:require [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]
            [source.db.util :as db.util]
            [source.prandom.core :as prandom])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(defn get-bundle-categories
  "Get all categories for feeds/posts in bundle"
  [ds {:keys [bundle-id content-type-id]}]
  (->> (-> (hsql/select-distinct :c.*)
           (hsql/from [:categories :c])
           (hsql/join [:feed-categories :fc] [:= :c.id :fc.category-id])
           (hsql/join [(:tname (db.util/tname :outgoing-posts bundle-id)) :p] [:= :fc.feed-id :p.feed-id])
           (hsql/group-by :c.id :c.name :c.display-picture)
           (hsql/where (when content-type-id [:= :p.content-type-id content-type-id]))
           (hsql/having (when (nil? content-type-id)
                          [:=
                           [:count [:distinct :p.content-type-id]]
                           (-> (hsql/select [[:count :id]])
                               (hsql/from :content-types))])))
       (hon/execute! ds)))

(defn get-outgoing-feeds
  "Gets a filtered list of outgoing feeds for the associated bundle."
  [ds {:keys [bundle-id type latest category-ids nonfiltered]}]
  (let [filtered-query (-> (hsql/select-distinct :f.*)
                           (hsql/from [:feeds :f])
                           (hsql/join [(:tname (db.util/tname :outgoing-posts bundle-id)) :p] [:= :f.id :p.feed-id])
                           (hsql/join [:feed-categories :fc] [:= :fc.feed-id :f.id])
                           (hsql/where
                            (when (nil? nonfiltered) [:not-in :f.id (-> (hsql/select :feed-id)
                                                                        (hsql/from :filtered-feeds)
                                                                        (hsql/where [:= :bundle-id bundle-id]))])
                            (when type [:= :f.content-type-id type])
                            (when (seq category-ids) [:in :fc.category-id category-ids])))
        filtered-feeds (hon/execute! ds (if (some? latest)
                                          (assoc filtered-query :order-by [[:created-at :desc]])
                                          filtered-query))]
    filtered-feeds))

(defn get-outgoing-posts
  "Get outgoing posts based on short heuristics and update analytics impressions"
  [ds {:keys [bundle-id limit start type latest category-ids seed]}]
  (let [filtered-posts (hon/execute!
                        ds
                        (-> (hsql/select-distinct :p.* [:f.title :feed-title])
                            (hsql/from [(:tname (db.util/tname :outgoing-posts bundle-id)) :p])
                            (hsql/join [:feeds :f] [:= :p.feed-id :f.id])
                            (hsql/join [:feed-categories :fc] [:= :p.feed-id :fc.feed-id])
                            (hsql/join [:categories :c] [:= :fc.category-id :c.id])
                            (hsql/where
                             (when type [:= :content-type-id type])
                             [:not-in :p.id (-> (hsql/select :post-id)
                                                (hsql/from :filtered-posts)
                                                (hsql/where [:= :bundle-id bundle-id]))]
                             [:not-in :p.feed-id (-> (hsql/select :feed-id)
                                                     (hsql/from :filtered-feeds)
                                                     (hsql/where [:= :bundle-id bundle-id]))]
                             (when (seq category-ids) [:in :c.id category-ids]))
                            (hsql/order-by [:p.posted-at :desc])))

        order-map (->> (if (or (nil? seed) (= seed ""))
                         (.format (LocalDateTime/now) (DateTimeFormatter/ofPattern "yyyy-MM-dd HH"))
                         seed)
                       (prandom/seeded-shuffle (count filtered-posts))
                       (map-indexed (fn [i item] [item i]))
                       (into {}))

        shuffled-posts (if (= latest "true")
                         filtered-posts
                         (->> (zipmap (-> filtered-posts count inc range) filtered-posts)
                              (sort-by #(get order-map (first %)))
                              (mapv last)))

        total-size (count shuffled-posts)

        valid-start? (and (some? start) (>= start 0) (< start total-size))
        started-posts (if valid-start?
                        (subvec shuffled-posts start)
                        shuffled-posts)

        valid-limit? (and (some? limit) (> (count started-posts) limit))
        limited-posts (if valid-limit?
                        (subvec started-posts 0 limit)
                        started-posts)

        next-index (when (and start limit) (+ start limit))]

    {:pagination {:page-size (count limited-posts)
                  :total-size total-size
                  :current-index start
                  :next-index (when (and next-index (< next-index total-size)) next-index)}
     :data limited-posts}))

(comment

  (time (get-outgoing-posts (db.util/conn) {:bundle-id 14
                                            :category-ids [50 52 54]
                                            :latest "false"}))

  (time (get-bundle-categories (db.util/conn) {:bundle-id 14}))

  ())
