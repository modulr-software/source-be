(ns source.db.interface
  (:require [source.db.util :as db.util]
            [source.db.honey :as hon]))

(defn ds [dbname]
  (db.util/conn dbname))

(defn db-name [& args]
  (apply db.util/db-name args))

(defn execute! [ds opts]
  (hon/execute! ds opts))

(defn find [ds opts]
  (hon/find ds opts))

(defn find-id 
  ([id target-id]
   (find-id (db.util/conn :master))))

(defn update! [ds opts]
  (hon/update! ds opts))

(defn delete! [ds opts]
  (hon/delete! ds opts))

(defn insert! [ds opts]
  (hon/insert! ds opts))

