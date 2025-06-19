(ns source.migrations.002-add-sector-joins-business-tables
  (:require [source.db.master.businesses :as businesses]
            [source.db.master.feeds-sectors :as feeds-sectors]
            [source.db.master.users-sectors :as users-sectors]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (businesses/create-table! ds-master)
    (users-sectors/create-table! ds-master)
    (feeds-sectors/create-table! ds-master)))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (businesses/drop-table! ds-master)
    (users-sectors/drop-table! ds-master)
    (feeds-sectors/drop-table! ds-master)))

