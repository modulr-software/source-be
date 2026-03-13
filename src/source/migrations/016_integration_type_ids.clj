(ns source.migrations.016-integration-type-ids
  (:require [source.db.master]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :bundles)
         (hsql/drop-column :integration-id)))
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :bundles)
         (hsql/add-column :integration-type-id :int)))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :bundles)
         (hsql/add-column :integration-id)))
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :bundles)
         (hsql/drop-column :integration-type-id)))))
