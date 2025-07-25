(ns source.services.xml-schemas
  (:require [source.db.interface :as db]
            [source.datastore.interface :as store]
            [source.rss.core :as rss]))

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

(defn ast
  [url]
  (-> url
      slurp
      rss/get-ast
      rss/collect-leaf-paths))

(defn extract-data
  [store schema-id url]
  (let [schema (store/find store {:tname :selection-schemas
                                  :key schema-id})]
    (println (store/get-all store {:tname :selection-schemas}))
    (println "schema: " schema)
    (->> url
         slurp
         rss/get-ast
         (rss/extract-data schema))))
