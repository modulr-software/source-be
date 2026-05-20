(ns source.migrations.021-channel-bundle-id
  (:require [source.db.master]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :integration-channels)
         (hsql/add-column :bundle-id :integer :not nil)))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :integration-channels)
         (hsql/drop-column :bundle-id)))))
