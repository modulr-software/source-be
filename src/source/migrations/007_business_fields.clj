(ns source.migrations.007-business-fields
  (:require [source.db.master]
            [source.db.honey :as hon]
            [source.db.tables :as tables]
            [honey.sql.helpers :as hsql]))

(def business-types
  [{:name "Independent"}
   {:name "Commercial"}
   {:name "Non-profit"}
   {:name "State-owned"}])

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (tables/create-table!
     ds-master
     :source.db.master
     :business-types)

    (hon/execute!
     ds-master
     (-> (hsql/alter-table :businesses)
         (hsql/add-column :business-type-id :integer)
         (hsql/add-column :registration :text)))))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/execute!
     ds-master
     (-> (hsql/alter-table :businesses)
         (hsql/drop-column :business-type-id :registration)))

    (tables/drop-table!
     ds-master
     :business-types)))
