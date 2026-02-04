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
            [next.jdbc :as jdbc]))

(defn create-integration! [ds {:keys [user-id bundle-metadata categories content-types]}]
  (let [new-bundle (bundles/create-bundle! ds {:user-id user-id
                                               :bundle-metadata bundle-metadata})]
    (jdbc/execute! ds [(str "CREATE DATABASE bundle_" (:id new-bundle))])
    (migrate/migrate-bundle (:id new-bundle) ["up"])

    (with-open [bundle-ds (db.util/conn :bundle (:id new-bundle))]
      (bundle-categories/insert-bundle-categories! bundle-ds {:bundle-id (:id new-bundle)
                                                              :categories categories})
      (bundle-content-types/insert-bundle-content-types! ds {:bundle-id (:id new-bundle)
                                                             :content-types content-types}))
    new-bundle))

(defn update-integration! [ds {:keys [bundle-id bundle-metadata categories content-types]}]
  (with-open [bundle-ds (db.util/conn :bundle bundle-id)]
    (bundles/update-bundle! ds {:id bundle-id
                                :data bundle-metadata})
    (bundle-categories/update-bundle-categories! bundle-ds {:bundle-id bundle-id
                                                            :categories categories})
    (bundle-content-types/update-bundle-content-types! ds {:bundle-id bundle-id
                                                           :content-types content-types})))

(defn hard-delete-bundle! [ds js job-id bundle-id]
  (hon/delete! ds {:tname :filtered-feeds
                   :where [:= :bundle-id bundle-id]})
  (hon/delete! ds {:tname :filtered-posts
                   :where [:= :bundle-id bundle-id]})
  (hon/delete! ds {:tname :bundle-content-types
                   :where [:= :bundle-id bundle-id]})
  (hon/delete! ds {:tname :events
                   :where [:= :bundle-id bundle-id]})
  (tables/drop-all-tables! (db.util/conn :bundle bundle-id))
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
