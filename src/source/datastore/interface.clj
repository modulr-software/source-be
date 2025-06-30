(ns source.datastore.interface
  (:require [source.datastore.datalevin :as dl]
            [source.datastore.util :as store.util]))

(defn ds [store-name]
  (store.util/conn store-name))

(defn store-name [& args]
  (apply store.util/store-name args))

(defn find [ds opts]
  (dl/find ds opts))

(defn exists? [ds opts]
  (dl/exists? ds opts))

(defn insert! [ds opts]
  (dl/insert! ds opts))

(defn update! [ds opts]
  (dl/update! ds opts))

(defn get-all [ds opts]
  (dl/get-all ds opts))

(defn delete! [ds opts]
  (dl/delete! ds opts))

(defn entries [ds opts]
  (dl/entries ds opts))
