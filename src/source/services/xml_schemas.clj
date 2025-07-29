(ns source.services.xml-schemas
  (:require [source.db.interface :as db]
            [source.datastore.interface :as store]
            [source.rss.core :as rss]))

(defn add-output-schema!
  [store schema]
  (let [id (-> (store/entries store :output-schemas/id)
               (count)
               (inc))]
    (store/insert! store {:output-schemas/id id
                          :output-schemas/schema schema})))

(defn output-schemas
  [store]
  (store/entities-with store :output-schemas/id))

(defn output-schema
  [store output-schema-id]
  (->> {:key :output-schemas/id
        :value output-schema-id}
       (store/find-entities store)
       (first)))

(defn add-selection-schema!
  [store db {:keys [schema record]}]
  (let [db-result (db/insert! db {:tname :selection-schemas
                                  :data record
                                  :ret :1})]
    (store/insert! store {:selection-schemas/id (:id db-result)
                          :selection-schemas/schema schema})))

(defn selection-schemas
  ([ds] (selection-schemas ds {}))
  ([ds opts]
   (->> {:tname :selection-schemas
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn selection-schemas-by-provider
  [ds {:keys [provider-id] :as opts}]
  (->> {:tname :selection-schemas
        :where [:= :provider-id provider-id]
        :ret :*}
       (merge opts)
       (db/find ds)))

(defn selection-schema [ds {:keys [id where] :as opts}]
  (->> {:tname :selection-schemas
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(defn add-provider!
  [store name]
  (let [id (-> (store/entries store :providers/id)
               (count)
               (inc))]
    (store/insert! store {:providers/id id
                          :providers/name name})))

(defn providers
  [store]
  (store/entities-with store :providers/id))

(defn provider
  [store provider-id]
  (->> {:key :providers/id
        :value provider-id}
       (store/find-entities store)
       (first)))

(defn ast
  [url]
  (-> url
      (slurp)
      (rss/get-ast)
      (rss/collect-leaf-paths)))

(defn extract-data
  [store schema-id url]
  (let [schema (->> {:key :selection-schemas/id
                     :value schema-id}
                    (store/find-entities store)
                    (first)
                    (:selection-schemas/schema))]
    (->> url
         (slurp)
         (rss/get-ast)
         (rss/extract-data schema))))

(comment
  (require '[source.db.util :as db.util])

  (def ds (store/ds :datahike))

  (selection-schemas-by-provider (db.util/conn) {:provider-id 1})

  (count (store/entries ds :selection-schemas/id))
  (store/entities-with ds :selection-schemas/id)
  (store/find-entities ds {:key :selection-schemas/id
                           :value 100})

  (extract-data
   ds
   1
   "https://www.youtube.com/feeds/videos.xml?channel_id=UCUyeluBRhGPCW4rPe_UvBZQ")

  (add-selection-schema!
   ds
   (db.util/conn)
   {:record {:provider-id 1
             :output-schema-id 1}
    :schema {:title {:path ["tag/body" "tag/feed" "tag/title" "content/0"]}}})

  ())
