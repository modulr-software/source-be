(ns source.migrations.010-selection-schema
  (:require [source.db.master]
            [source.db.honey :as hon]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (tables/create-table!
     ds-master
     :source.db.master
     :output-schemas)

    (->> {:alter-table :selection-schemas
          :add-column [:schema :text]}
         (hon/execute! ds-master))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (->> {:alter-table :selection-schemas
          :drop-column :schema}
         (hon/execute! ds-master))

    (tables/drop-table! ds-master :output-schemas)))
