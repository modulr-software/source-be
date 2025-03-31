(ns source.db.core
  (:require [source.password :as pw]
            [next.jdbc :as jdbc]))

(def scaffold-query ["CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, password TEXT)"])

(def ^:private db-config {:dbname "source-users" :dbtype "sqlite"})

(defn db []
  (jdbc/get-datasource db-config))

(defn ds [conf]
  (jdbc/get-datasource conf))

(defn- -reset-db! [ds]
  (jdbc/execute! ds ["DROP TABLE IF EXISTS users"])
  (jdbc/execute! ds scaffold-query))


  

