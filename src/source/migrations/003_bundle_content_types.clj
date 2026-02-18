(ns source.migrations.003-bundle-content-types
  (:require [source.db.master]
            [source.db.tables :as tables]
            [source.services.interface :as services]))

(defn run-up! [context]
  (let [ds-master (:db-master context)
        bundles (services/bundles ds-master)]

    (tables/create-tables!
     ds-master
     :source.db.master
     [:bundle-content-types])

    (run! (fn [{:keys [id content-type-id]}]
            (when (some? content-type-id)
              (services/insert-bundle-content-types! ds-master {:bundle-id id
                                                                :content-types [{:id content-type-id}]})))
          bundles)))

(defn run-down! [context]
  (let [ds-master (:db-master context)
        bundle-content-types (services/bundles ds-master)]

    (run! (fn [{:keys [bundle-id content-type-id]}]
            (services/update-bundle! ds-master {:id bundle-id
                                                :data {:content-type-id content-type-id}}))
          bundle-content-types)

    (tables/drop-table! ds-master :bundle-content-types)))
