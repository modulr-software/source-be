(ns source.migrations.015-integration-types
  (:require [source.db.master]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (tables/create-table!
     ds-master
     :source.db.master
     :integration-types)

    (hon/insert! ds-master {:tname :integration-types
                            :data [{:name "Website"}
                                   {:name "App"}
                                   {:name "Plugin"}
                                   {:name "Community"}]})

    (hon/execute!
     ds-master
     (-> (hsql/alter-table :bundles)
         (hsql/add-column :integration-id :int)))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :bundles)
         (hsql/drop-column :integration-id)))

    (tables/drop-table! ds-master :integration-types)))
