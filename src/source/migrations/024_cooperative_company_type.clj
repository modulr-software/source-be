(ns source.migrations.024-cooperative-company-type
  (:require [source.db.master]
            [source.db.honey :as hon]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (hon/insert! ds-master {:tname :business-types
                            :data [{:name "Cooperative"}]})))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/delete! ds-master {:tname :business-types
                            :where [:= :name "Cooperative"]})))
