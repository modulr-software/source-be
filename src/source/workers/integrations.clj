(ns source.workers.integrations
  (:require [source.services.bundles :as bundles]
            [source.migrate :as migrate]
            [source.db.util :as db.util]
            [source.services.bundle-categories :as bundle-categories]
            [source.services.bundle-content-types :as bundle-content-types]
            [source.jobs.core :as jobs]
            [source.jobs.handlers :as handlers]
            [source.util :as utils]
            [congest.jobs :as congest]
            [source.db.tables :as tables]
            [source.db.honey :as hon]))

(defn create-integration! [ds js store {:keys [user-id bundle-metadata categories content-types]}]
  (let [new-bundle (bundles/create-bundle! ds {:user-id user-id
                                               :bundle-metadata bundle-metadata})
        _ (migrate/migrate-bundle (:id new-bundle) ["up"])]

    (with-open [bundle-ds (db.util/conn :bundle (:id new-bundle))]
      (let [_ (bundle-categories/insert-bundle-categories! bundle-ds {:bundle-id (:id new-bundle)
                                                                      :categories categories})
            _ (bundle-content-types/insert-bundle-content-types! ds {:bundle-id (:id new-bundle)
                                                                     :content-types content-types})

            categories-by-bundle (bundles/categories-in-bundle ds (:id new-bundle))]

        ; service needed
        (->> (jobs/prepare-congest-metadata
              ds
              store
              {:id (handlers/update-bundle-job-id (:id new-bundle))
               :initial-delay 0
               :auto-start true
               :stop-after-fail false,
               :interval (* 1000 60 60 24)
               :recurring? true
               :ds ds
               :args {:bundle-id (:id new-bundle)
                      :categories categories-by-bundle}
               :handler :update-bundle
               :created-at (utils/get-utc-timestamp-string)
               :sleep false})
             (congest/register! js))))
    new-bundle))

(defn update-integration! [ds js store {:keys [bundle-id bundle-metadata categories content-types]}]
  (with-open [bundle-ds (db.util/conn :bundle bundle-id)]
    (let [_ (bundles/update-bundle! ds {:id bundle-id
                                        :data bundle-metadata})

          job-id (str "bundle_" bundle-id)

        ; update bundle categories
          _ (bundle-categories/update-bundle-categories! bundle-ds {:bundle-id bundle-id
                                                                    :categories categories})
        ; update bundle content types
          _ (bundle-content-types/update-bundle-content-types! ds {:bundle-id bundle-id
                                                                   :content-types content-types})

          categories-by-bundle (bundles/categories-in-bundle ds bundle-id)]

      ; service needed
      (congest/deregister! js job-id)
      (->> (jobs/prepare-congest-metadata
            ds
            store
            {:id job-id
             :initial-delay (* 1000 60 60 24)
             :auto-start true
             :stop-after-fail false,
             :interval (* 1000 60 60 24)
             :recurring? true
             :ds ds
             :args {:bundle-id bundle-id
                    :categories categories-by-bundle}
             :handler :update-bundle
             :created-at (utils/get-utc-timestamp-string)
             :sleep false})
           (congest/register! js)))))

(defn hard-delete-bundle! [ds js bundle-id]
  (let [job-id (handlers/update-bundle-job-id bundle-id)]
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
    (congest/deregister! js job-id)))

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
