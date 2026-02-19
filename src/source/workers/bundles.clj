(ns source.workers.bundles
  (:require [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]
            [clojure.set :as set]
            [source.services.feed-categories :as feed-categories]
            [source.db.util :as db.util]
            [source.prandom.core :as prandom]
            [honey.sql :as sql])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(defn get-bundle-categories
  "Get all categories for feeds/posts in bundle"
  [ds bundle-id]
  (let [feed-ids (->> (hon/find ds (db.util/tname :outgoing-posts bundle-id))
                      (mapv :feed-id))
        category-ids (->> (hon/find ds {:tname :feed-categories
                                        :where (when (seq feed-ids) [:in :feed-id feed-ids])
                                        :ret :*})
                          (mapv :category-id))]
    (hon/find ds {:tname :categories
                  :where (when (seq category-ids) [:in :id category-ids])
                  :ret :*})))

(defn get-outgoing-feeds
  "Gets a filtered list of outgoing feeds for the associated bundle."
  [ds {:keys [bundle-id type latest category-ids nonfiltered]}]
  (let [feed-ids (mapv :feed-id (hon/find ds (db.util/tname :outgoing-posts bundle-id)))
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
                              (when (seq category-filtered-feed-ids) [:in :id category-filtered-feed-ids])
                              (when (seq blocked-feed-ids) [:not [:in :id blocked-feed-ids]]))
                  (assoc :order-by (when latest [[:created-at :desc]]))
                  (merge {:tname :feeds
                          :ret :*}))
        type-filtered (hon/find ds query)]
    type-filtered))

(defn get-outgoing-posts
  "Get outgoing posts based on short heuristics and update analytics impressions"
  [ds {:keys [bundle-id limit start type latest category-ids seed]}]
  (let [all-feed-ids (mapv :id (hon/find ds {:tname :feeds
                                             :ret :*}))
        blocked-feed-ids (mapv :feed-id (hon/find ds {:tname :filtered-feeds
                                                      :where [:= :bundle-id bundle-id]
                                                      :ret :*}))
        available-feed-ids (vec (remove (set blocked-feed-ids) all-feed-ids))

        blocked-post-ids (mapv :post-id (hon/find ds {:tname :filtered-posts
                                                      :where [:= :bundle-id bundle-id]
                                                      :ret :*}))

        filtered-posts (hon/execute!
                        ds
                        (-> (hsql/select :p.*)
                            (hsql/from [(:tname (db.util/tname :outgoing-posts bundle-id)) :p])
                            (hsql/join [:feed-categories :fc] [:= :p.feed-id :fc.feed-id])
                            (hsql/join [:categories :c] [:= :fc.category-id :c.id])
                            (hsql/where
                             (when type [:= :content-type-id type])
                             (when (seq blocked-post-ids) [:not [:in :p.id blocked-post-ids]])
                             (when (seq available-feed-ids) [:in :p.feed-id available-feed-ids])
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

        valid-start? (and (some? start) (>= start 0) (< start (count shuffled-posts)))
        started-posts (if valid-start?
                        (subvec shuffled-posts start)
                        shuffled-posts)

        valid-limit? (and (some? limit) (> (count started-posts) limit))
        limited-posts (if valid-limit?
                        (subvec started-posts 0 limit)
                        started-posts)]
    limited-posts))

(comment 

  (time (get-outgoing-posts (db.util/conn) {:bundle-id 14
                                            :category-ids [50 52 54]
                                            :latest "false"}))
  
  ())
