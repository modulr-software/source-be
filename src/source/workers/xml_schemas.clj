(ns source.workers.xml-schemas
  (:require [clojure.data.json :as json]
            [source.db.honey :as hon]
            [source.rss.core :as rss]))

(defn insert-output-schema! [ds schema]
  (hon/insert! ds {:tname :output-schemas
                   :data {:schema (json/write-str schema)}}))

(defn output-schemas [ds]
  (->> (hon/find ds {:tname :output-schemas})
       (mapv #(assoc % :schema (json/read-str (:schema %) {:key-fn keyword})))))

(defn output-schema [ds id]
  (let [{:keys [schema] :as os} (hon/find-one ds {:tname :output-schemas
                                                  :where [:= :id id]})]
    (assoc os :schema (json/read-str (or schema "{}") {:key-fn keyword}))))

(defn insert-selection-schema!
  [ds {:keys [schema record]}]
  (let [{:keys [output-schema-id provider-id]} record]
    (hon/insert! ds {:tname :selection-schemas
                     :data {:schema (json/write-str schema)
                            :output-schema-id output-schema-id
                            :provider-id provider-id}
                     :ret :1})))

(defn delete-selection-schemas-by-provider!
  [ds provider-id]
  (hon/delete! ds {:tname :selection-schemas
                   :where [:= :provider-id provider-id]}))

(defn selection-schemas
  ([ds] (selection-schemas ds {}))
  ([ds opts]
   (->> {:tname :selection-schemas
         :ret :*}
        (merge opts)
        (hon/find ds)
        (mapv #(assoc % :schema (json/read-str (or (:schema %) "{}") {:key-fn keyword}))))))

(defn selection-schema [ds {:keys [id] :as opts}]
  (let [{:keys [schema] :as selection-schema} (->> {:tname :selection-schemas
                                                    :where [:= :id id]}
                                                   (merge opts)
                                                   (hon/find-one ds))
        schema' (json/read-str (or schema "{}") {:key-fn keyword})]
    (assoc selection-schema :schema schema')))

(defn ast
  [url]
  (-> url
      (slurp)
      (rss/get-ast)
      (rss/collect-leaf-paths)))

(defn extract-data
  [ds schema-id url]
  (let [json (->> {:tname :selection-schemas
                   :where [:= :id schema-id]}
                  (hon/find-one ds)
                  (:schema))
        schema (json/read-str (or json "{}") {:key-fn keyword})]
    (->> url
         (slurp)
         (rss/get-ast)
         (rss/extract-data schema))))
