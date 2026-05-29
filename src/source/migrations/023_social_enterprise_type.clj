(ns source.migrations.023-social-enterprise-type
  (:require [source.db.master]
            [source.db.honey :as hon]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (hon/insert! ds-master {:tname :business-types
                            :data [{:name "Social Enterprise"}]})))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/delete! ds-master {:tname :business-types
                            :where [:= :name "Social Enterprise"]})))
