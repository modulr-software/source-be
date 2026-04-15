(ns source.migrations.004-category-images
  (:require [source.db.master]
            [source.db.honey :as hon]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (->> {:alter-table :categories
          :add-column [:display-picture :text]}
         (hon/execute! ds-master))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (->> {:alter-table :categories
          :drop-column :display-picture}
         (hon/execute! ds-master))))
