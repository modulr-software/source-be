(ns source.migrations.009-user-removed
  (:require [source.db.master]
            [source.db.honey :as hon]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (->> {:alter-table :users
          :add-column [:removed :integer [:default 0]]}
         (hon/execute! ds-master))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (->> {:alter-table :users
          :drop-column :removed}
         (hon/execute! ds-master))))
