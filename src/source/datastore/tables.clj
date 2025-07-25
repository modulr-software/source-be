(ns source.datastore.tables
  (:require [datalevin.core :as d]))

(defn open-table!
  "Opens table in the store."
  [store tname]
  (d/open-dbi store (name tname)))

(defn drop-table!
  "Clears data from and deleted table"
  [store tname]
  (d/drop-dbi store (name tname)))

(defn clear-table!
  "Clear data from table"
  [store tname]
  (d/clear-dbi store (name tname)))

(defn tables
  "Returns a vector of all tables in store"
  [store]
  (d/list-dbis store))

(open-table! (d/open-kv "store") "some-table")
