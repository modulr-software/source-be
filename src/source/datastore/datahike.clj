(ns source.datastore.datahike
  (:require [datahike.api :as d]
            [source.util :as util]))

(defn lookup
  "Returns one or more entities associated with the provided entity id(s)"
  [ds ids]
  (let [multi? (vector? ids)
        ids-vec (if multi? ids [ids])]
    (mapv (fn [id]
            (->> (d/entity @ds id)
                 (into {}))) ids-vec)))

(defn find
  "Returns one ore more entity ids where the provided key matches the provided value"
  [ds {:keys [key value]}]
  (->> (d/q '[:find ?e
              :in $ ?k ?v
              :where [?e ?k ?v]]
            @ds key value)
       (vec)
       (flatten)
       (into [])))

(defn find-entities
  "Same as find, but returns a vector of entities"
  [ds {:keys [_key _value] :as query}]
  (->> query
       (find ds)
       (lookup ds)))

(defn exists?
  "Returns true if value exists for key in kv-store"
  [ds k]
  (-> (d/q '[:find ?e
             :in $ ?k
             :where [?e ?k _]]
           @ds k)
      (seq)
      (boolean)))

(defn entries
  "Get eids of all entities in which the provided attribute is present"
  [ds key]
  (->> (d/q '[:find ?e
              :in $ ?k
              :where [?e ?k _]]
            @ds key)
       (vec)
       (flatten)
       (into [])))

(defn entities-with
  "Gets all entities in which the provided attribute is present"
  [ds key]
  (->> key
       (entries ds)
       (lookup ds)))

(defn delete!
  "Accepts an entity id or a vec of entity ids and removes all the entities associated thereby"
  [ds ids]
  (let [multi? (vector? ids)
        ids-vec (if multi? ids [ids])
        query (mapv (fn [id] [:db/retractEntity id]) ids-vec)]
    (d/transact ds query)))

(defn insert!
  "Inserts kv's into store. Returns the key-value pairs that were inserted."
  [ds data]
  (let [multi? (util/vectors? data)
        input-kvs (if multi? data [data])]
    (d/transact ds input-kvs)
    input-kvs))

(defn update!
  "Update attributes and values for a given entity"
  [ds id data]
  (->> (merge {:db/add id} data)
       (vec)
       (flatten)
       (conj [])
       (d/transact ds)))

(defn get-all
  "Returns all key value pairs in table in kv-store."
  [ds]
  (->> (d/q '[:find ?e :where [?e _ _]] @ds)
       (map (comp (partial into {}) (partial d/entity @ds) first))))

(comment
  (require '[source.datastore.util :as ds.util])

  (def ds (ds.util/conn :datahike))
  (get-all ds)
  (entries ds :user/name)
  (delete! ds (entries ds :user/name))
  (update! ds 3 {:user/age 21})

  (exists? ds :user/name)
  (lookup ds [6 7])
  (find ds {:key :user/name
            :value "Keagan"})
  (find-entities ds {:key :user/age
                     :value 23})
  (insert! ds {:user/name "Shani"
               :user/age 23})

  (insert! ds {:selection-schemas/id 4
               :selection-schemas/schema {:title {:path ["tag/body" "tag/feed" "tag/title" "content/0"]}}})
  (get-all ds)
  (find ds {:key :selection-schemas/id
            :value 4})

  (let [ds (ds.util/conn :datahike)
        id :selection-schemas/id
        k :selection-schemas/schema
        v {:title {:path ["tag/body" "tag/feed" "tag/title" "content/0"]}}]
    (println (get-all ds))
    (println (entities-with ds k))
    (println "Putting a value")
    (insert! ds {id 1
                 k v})
    (assert (= v
               (->> k
                    (entities-with k)
                    (first)
                    (k))))
    (insert! ds {k v})
    (println "1 " (get-all ds))
    (println "2 " (find ds {:key id
                            :value 1}))
    (println "3 " (get-all ds))
    (println "4 " (find ds {:key id
                            :value 1}))
    (println "Test passed")
    (println "Deleting a value")
    (delete! ds (entries ds k))
    (assert (= []
               (entries ds k)))
    (println "Test passed"))

  ())
