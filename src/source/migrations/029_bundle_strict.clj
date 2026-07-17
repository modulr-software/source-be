(ns source.migrations.029-bundle-strict
  (:require [source.db.master]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :bundles)
         (hsql/add-column :strict :integer :not nil [:default 0])))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :bundles)
         (hsql/drop-column :strict)))))
