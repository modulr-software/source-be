(ns source.migrations.018-sector-seeding
  (:require [source.db.master]
            [source.db.honey :as hon]))

(defn run-up! [context]
  (let [ds-master (:db-master context)
        sectors (->> (hon/find ds-master {:tname :categories})
                     (mapv #(dissoc % :display-picture)))]
    (hon/delete! ds-master {:tname :sectors})
    (hon/insert! ds-master {:tname :sectors
                            :data sectors})))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (hon/delete! ds-master {:tname :sectors})
    (hon/insert! ds-master {:tname :sectors
                            :data [{:name "renewable energy"}
                                   {:name "conservation ecology"}
                                   {:name "recycling"}]})))
