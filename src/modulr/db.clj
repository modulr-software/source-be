(ns modulr.db
  (:require [next.jdbc :as jdbc]))

(def scaffold-query ["CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT)"])

(def ^:private db-config {:dbname "source-users" :dbtype "sqlite"})
(defn get-db []
  (jdbc/get-datasource db-config))

(get-db)

(jdbc/execute! (get-db) scaffold-query)
