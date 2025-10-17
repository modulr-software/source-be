(ns source.migrations.003-bundle-content-types
  (:require [source.db.master]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (tables/create-tables!
     ds-master
     :source.db.master
     [:bundle-content-types])))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (tables/drop-table! ds-master :bundle-content-types)))
