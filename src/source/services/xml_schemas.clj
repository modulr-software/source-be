(ns source.services.xml-schemas
  (:require [source.datastore.interface :as store]
            [source.db.interface :as db]
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

(defn highest-version
  [previous-records]
  (->> (reduce (fn [acc {:keys [version]}]
                 (conj acc version)) [] previous-records)
       (apply max 0)))

(defn add-selection-schema!
  [store db {:keys [schema record]}]
  (let [{:keys [output-schema-id provider-id]} record
        previous-versions (db/find db {:tname :selection-schemas
                                       :where [:= :provider-id provider-id]
                                       :ret :*})
        next-version (-> (highest-version previous-versions)
                         (inc))
        db-result (db/insert! db {:tname :selection-schemas
                                  :data {:output-schema-id output-schema-id
                                         :provider-id provider-id
                                         :version next-version}
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

(defn delete-provider!
  [store provider-id]
  (->> {:key :providers/id
        :value provider-id}
       (store/find store)
       (store/delete! store)))

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
  (ast "https://www.youtube.com/feeds/videos.xml?channel_id=UCUyeluBRhGPCW4rPe_UvBZQ")

  (reduce (fn [acc {:keys [version]}]
            (conj acc version)) [] [{:version 2} {:version 3}])

  (db/find (db.util/conn) {:tname :selection-schemas
                           :where [:and
                                   [:= :output-schema-id 1]
                                   [:= :provider-id 1]]
                           :ret :*})

  (def ds (store/ds :datahike))

  (selection-schemas-by-provider (db.util/conn) {:provider-id 1})
  (add-provider! ds "poop")
  (add-provider! ds "YouTube")
  (providers ds)

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
