(ns source.workers.integrations
  (:require [source.services.bundles :as bundles]
            [source.migrate :as migrate]
            [source.db.util :as db.util]
            [source.services.bundle-categories :as bundle-categories]
            [source.services.bundle-content-types :as bundle-content-types]
            [source.util :as utils]
            [source.db.tables :as tables]
            [source.db.honey :as hon]
            [congest.jobs :as congest]
            [source.db.bundle :as bundle]))

(defn create-integration! [ds {:keys [user-id bundle-metadata categories content-types]}]
  (let [new-bundle (bundles/create-bundle! ds {:user-id user-id
                                               :bundle-metadata bundle-metadata})]
    (migrate/migrate-bundle (:id new-bundle) ["up"])

    (bundle-categories/insert-bundle-categories! ds {:bundle-id (:id new-bundle)
                                                     :categories categories})
    (bundle-content-types/insert-bundle-content-types! ds {:bundle-id (:id new-bundle)
                                                           :content-types content-types})
    new-bundle))

(defn update-integration! [ds {:keys [bundle-id bundle-metadata categories content-types]}]
  (bundles/update-bundle! ds {:id bundle-id
                              :data bundle-metadata})
  (bundle-categories/update-bundle-categories! ds {:bundle-id bundle-id
                                                   :categories categories})
  (bundle-content-types/update-bundle-content-types! ds {:bundle-id bundle-id
                                                         :content-types content-types}))

(defn hard-delete-bundle! [ds js job-id bundle-id]
  (hon/delete! ds {:tname :filtered-feeds
                   :where [:= :bundle-id bundle-id]})
  (hon/delete! ds {:tname :filtered-posts
                   :where [:= :bundle-id bundle-id]})
  (hon/delete! ds {:tname :bundle-content-types
                   :where [:= :bundle-id bundle-id]})
  (hon/delete! ds {:tname :events
                   :where [:= :bundle-id bundle-id]})
  (tables/drop-tables! ds (bundle/tnames [:outgoing-posts
                                          :bundle-categories
                                          :post-heuristics]
                                         bundle-id))
  (hon/delete! ds {:tname :bundles
                   :where [:= :id bundle-id]})
  (congest/deregister! js job-id))

(defn generate-api-key! [ds user-id bundle-id]
  (let [uuid (utils/uuid)
        api-key (utils/sha256 (str user-id bundle-id uuid))]
    (hon/update! ds {:tname :bundles
                     :where [:= :id bundle-id]
                     :data {:hash api-key}})
    api-key))

(defn update-filtered-feeds! [ds {:keys [filtered bundle-id feed-id]}]
  (if filtered
    (hon/insert! ds {:tname :filtered-feeds
                     :data {:feed-id feed-id
                            :bundle-id bundle-id}})
    (hon/delete! ds {:tname :filtered-feeds
                     :where [:and
                             [:= :feed-id feed-id]
                             [:= :bundle-id bundle-id]]})))

(defn update-filtered-posts! [ds {:keys [filtered bundle-id post-id]}]
  (if filtered
    (hon/insert! ds {:tname :filtered-posts
                     :data {:post-id post-id
                            :bundle-id bundle-id}})
    (hon/delete! ds {:tname :filtered-posts
                     :where [:and
                             [:= :post-id post-id]
                             [:= :bundle-id bundle-id]]})))
