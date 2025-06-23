(ns source.db.interface
  (:require [source.db.util :as db.util]))

(defn ds [dbname]
  (db.util/conn dbname))

(defn creator-id [& args]
  (apply db.util/creator-id args))

(defn db-name [& args]
  (apply db.util/db-name args))

