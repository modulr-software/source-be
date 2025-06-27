(ns source.datastore.datalevin
  (:require [datalevin.core :as d]))

;; Something to consider (async transactions) https://cljdoc.org/d/datalevin/datalevin/0.9.22/doc/transaction#asynchronous-transaction

(defn get-value [ds {:keys [tname key]}]
  (d/get-value ds (name tname) key))

(defn delete! [ds {:keys [tname keys]}]
  (->> (mapv (fn [key] [:del (name tname) key]) keys)
       (d/transact-kv ds)))

(defn put! [ds {:keys [tname data]}]
  (->> (mapv (fn [item] [:put (name tname) (first item) (last item)]) data)
       (d/transact-kv ds)))

(defn get-all
  [ds {:keys [tname]}]
  (let [tn (name tname)]
    (mapv (fn [[k v]] [k v])
          (d/entries ds tn))))

(comment
  (require '[source.datastore.util :as ds.util])
  (let [ds (ds.util/conn "store")
        key "somekey"
        value "somestringvalue"
        tname :some-table]
    (println "Putting a value")
    (d/open-dbi ds (name tname))
    (put! ds {:tname tname
              :data [[key value]]})
    (assert (= value
               (get-value ds {:tname tname
                              :key key})))
    (println "Test passed")
    (println "Deleting a value")
    (delete! ds {:tname tname
                 :keys [key]})
    (assert (= nil
               (get-value ds {:tname tname
                              :key key})))
    (ds.util/close ds)))
