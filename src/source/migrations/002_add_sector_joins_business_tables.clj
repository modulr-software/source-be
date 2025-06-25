(ns source.migrations.002-add-sector-joins-business-tables
  (:require [source.db.tables :as tables]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (tables/create-tables!
     ds-master
     :source.db.master
     [:businesses
      :users-sectors
      :feeds-sectors])))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (tables/drop-tables!
     ds-master
     [:businesses
      :users-sectors
      :feeds-sectors])))

