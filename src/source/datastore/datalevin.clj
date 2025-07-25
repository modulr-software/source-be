(ns source.datastore.datalevin
  (:require [datalevin.core :as d]
            [source.datastore.tables :as tables]
            [source.util :as util]))

;; If we want to have higher write speed in future we can use transact-kv-async

;; TODO:
;; - when operations on kv store are done the relevant tables need to be opened beforehand
;;   using (datalevin.core/open-dbi "table-name")
;; - we need a nice way of opening and closing a connection to the datastore before and after
;;  an operation is performed. Closing a connection to the datastore will also close the tables
;;  in that datastore. Although it isn't documented as far as I could see in the datalevin or
;;  LMDB docs, I think we should avoid opening the same table multiple times. So we need to figure
;;  out a way to open a connection once (both on a datastore level and a datastore level), reuse it
;;  where required, and then close it.
;;
;;  A possible solution here is to open up the kv-store and the tables we use on server start and
;;  pass the kv-store connection around. As you might notice from the implementation, we don't need
;;  to "def" a table connection and pass it around, only the store connection. As long as you call
;;  (datalevin.core/open-dbi "table-name"). Refer to the comment block below.

(defn find
  "Returns the value for a key in the kv-store"
  [ds {:keys [tname key]}]
  (tables/open-table! ds (name tname))
  (println "finding for " key " in " (name tname))
  (d/get-value ds (name tname) key))

(defn exists?
  "Returns true if value exists for key in kv-store"
  [ds opts]
  (-> (find ds opts)
      (some?)))

(defn entries
  "Get the number of entries in a table"
  [ds {:keys [tname]}]
  (tables/open-table! ds (name tname))
  (d/entries ds tname))

(defn delete!
  "Removes one or multiple keys from kv store."
  [ds {:keys [tname keys]}]
  (tables/open-table! ds (name tname))
  (->> (mapv (fn [key] [:del key]) keys)
       (d/transact-kv ds (name tname))))

(defn insert!
  "Inserts kv's into store. Skips keys that already exist.
  Returns the key-value pairs that were inserted."
  [ds {:keys [tname data]}]
  (tables/open-table! ds (name tname))
  (let [multi? (util/vectors? data)
        input-kvs (if multi? data [data])
        kvs-to-insert (->> input-kvs
                           (filterv (fn [[k _]]
                                      (not (exists? ds {:tname tname :key k})))))]
    (->> kvs-to-insert
         (mapv (fn [[k v]] [:put k v]))
         (d/transact-kv ds (name tname)))
    kvs-to-insert))

(defn update!
  "Replaces values for keys in store. Skips keys that don't exist"
  [ds {:keys [tname data]}]
  (tables/open-table! ds (name tname))
  (->> data
       (filter (fn [[k _]]
                 (exists? ds {:tname tname :key k})))
       (map (fn [[k v]]
              [:put k v]))
       (d/transact-kv ds (name tname))))

(defn get-all
  "Returns all key value pairs in table in kv-store."
  [ds {:keys [tname]}]
  (tables/open-table! ds (name tname))
  (println "finding all in " (name tname))
  (d/get-range ds (name tname) [:all]))

(comment
  (require '[source.datastore.util :as ds.util])
  (let [ds (ds.util/conn :store)
        key "test-key"
        value {:title {:path ["tag/body" "tag/feed" "tag/title" "content/0"]}}
        tname :test-table]
    (println (get-all ds {:tname :selection-schemas}))
    (println (find ds {:tname :selection-schemas :key 4}))
    (println "Putting a value")
    (d/open-dbi ds (name tname))
    (insert! ds {:tname tname
                 :data [key value]})
    (assert (= value
               (find ds {:tname tname
                         :key key})))
    (insert! ds {:tname tname
                 :data [key value]})
    (println "1 " (get-all ds {:tname :selection-schemas}))
    (println "2 " (find ds {:tname :selection-schemas :key 4}))
    (println "3 " (get-all ds {:tname tname}))
    (println "4 " (find ds {:tname tname :key "test-key"}))
    (println "Test passed")
    (println "Deleting a value")
    (delete! ds {:tname tname
                 :keys [key]})
    (assert (= nil
               (find ds {:tname tname
                         :key key})))
    (println "Test passed")
    ()
    (ds.util/close ds)))
