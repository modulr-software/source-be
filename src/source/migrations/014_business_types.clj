(ns source.migrations.014-business-types
  (:require [source.db.master]
            [source.db.honey :as hon]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (hon/insert! ds-master {:tname :business-types
                            :data [{:name "For-Profit"}
                                   {:name "Non-Profit"}
                                   {:name "Creator"}]})))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/delete! ds-master {:tname :business-types
                            :where [:or
                                    [:= :name "For-Profit"]
                                    [:= :name "Non-Profit"]
                                    [:= :name "Creator"]]})))
