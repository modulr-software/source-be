(ns source.db.util
  (:require [source.config :as conf]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn db-path [dbname]
  (str (conf/read-value :database :dir) dbname))

(defn conn [dbname]
  (-> {:dbtype (conf/read-value :database :type)}
      (merge {:dbname (db-path (name dbname))})
      (jdbc/get-connection)
      (jdbc/with-options {:builder-fn rs/as-unqualified-lower-maps})))

(defn db-name
  ([type]
   (name type))
  ([type id]
   (str (name type) "_" id)))

