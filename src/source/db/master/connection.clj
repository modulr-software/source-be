(ns source.db.master.connection
  (:require [source.db.util :as db.util]))

(def db-name "master")
(defn get-ds [] (db.util/conn db-name))
(def ds (db.util/conn db-name))
