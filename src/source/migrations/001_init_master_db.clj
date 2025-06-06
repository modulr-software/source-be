(ns source.migrations.001-init-master-db
  (:require [source.db.master.users :as users]
            [source.db.master.bundles :as bundles]
            [source.db.master.baselines :as baselines]
            [source.db.master.feeds :as feeds]
            [source.db.master.feeds-categories :as feeds-categories]
            [source.db.master.providers :as providers]
            [source.db.master.sectors :as sectors]
            [source.db.master.categories :as categories]
            [source.db.master.content-type :as content-types]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (users/create-users-table ds-master)
    (bundles/create-bundles-table ds-master)
    (baselines/create-baselines-table ds-master)
    (feeds/create-table ds-master)
    (feeds-categories/create-table ds-master)
    (providers/create-providers-table ds-master)
    (sectors/create-sectors-table ds-master)
    (categories/create-table ds-master)
    (content-types/create-content-types-table ds-master)))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (users/drop-users-table ds-master)
    (bundles/drop-bundles-table ds-master)
    (baselines/drop-baselines-table ds-master)
    (feeds/drop-table ds-master)
    (feeds-categories/drop-table ds-master)
    (providers/drop-providers-table ds-master)
    (sectors/drop-sectors-table ds-master)
    (categories/drop-table ds-master)
    (content-types/drop-content-types-table ds-master)))
