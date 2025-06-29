(ns source.services.xml-schemas
  (:require [source.db.interface :as db]
            [source.datastore.interface :as store]))

(defn get-all
  [ds {:keys [tname]}]
  (store/get-all ds {:tname tname}))

(defn add-selection-schema!
  [store db {:keys [schema record]}]
  (let [db-result (db/insert! db {:tname :selection-schemas
                                  :data record})]
    (println db-result)
    (store/insert! store {:tname :selection-schemas
                          :data [(:id db-result) schema]})))
