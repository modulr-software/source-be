(ns source.workers.bundles
  (:require [source.db.util :as db.util]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]
            [source.services.analytics.interface :as analytics]))

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
