(ns source.migrations.025-user-locations
  (:require [source.db.master]
            [source.db.honey :as hon]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [ds-master (:db-master context)
        users (hon/find ds-master {:tname :users})]

    (tables/create-table! ds-master :source.db.master :user-locations)

    (run!
     (fn [{:keys [id address]}]
       (when (and (some? address) (not (= address "")))
         (hon/insert! ds-master {:tname :user-locations
                                 :where [:= :id id]
                                 :data {:user-id id
                                        :location address}})))
     users)))

(defn run-down! [context]
  (let [ds-master (:db-master context)
        locations (hon/find ds-master {:tname :user-locations})]

    (run!
     (fn [{:keys [user-id location]}]
       (hon/update! ds-master {:tname :users
                               :where [:= :id user-id]
                               :data {:address location}}))
     locations)

    (tables/drop-table! ds-master :user-locations)))
