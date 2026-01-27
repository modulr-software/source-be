(ns source.workers.integrations
  (:require [source.services.bundles :as bundles]
            [source.migrate :as migrate]
            [source.db.util :as db.util]
            [source.services.bundle-categories :as bundle-categories]
            [source.services.bundle-content-types :as bundle-content-types]
            [source.jobs.core :as jobs]
            [source.jobs.handlers :as handlers]
            [source.util :as utils]
            [congest.jobs :as congest]))

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
