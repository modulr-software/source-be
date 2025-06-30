(ns source.services.xml-schemas
  (:require [source.db.interface :as db]
            [source.datastore.interface :as store]))

(defn get-all
  [ds {:keys [tname]}]
  (store/get-all ds {:tname tname}))

(defn add-output-schema!
  [store {:keys [schema]}]
  (let [id (-> (store/entries store {:tname :output-schemas})
               inc)]
    (store/insert! store {:tname :selection-schemas
                          :data [id schema]})))

(defn add-selection-schema!
  [store db {:keys [schema record]}]
  (let [db-result (db/insert! db {:tname :selection-schemas
                                  :data record})]
    (store/insert! store {:tname :selection-schemas
                          :data [(:id db-result) schema]})))

(defn selection-schemas
  ([ds] (selection-schemas ds {}))
  ([ds opts]
   (->> {:tname :selection-schemas}
        (merge opts)
        (db/find ds))))

(defn selection-schema [ds {:keys [id where] :as opts}]
  (->> {:tname :selection-schemas
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(defn output-schemas
  [store]
  (store/get-all store {:tname :output-schemas}))

(defn output-schema
  [store id]
  (store/find store {:tname :output-schemas
                     :key id}))

