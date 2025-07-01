(ns source.db.util
  (:require [source.config :as conf]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn db-path [dbname]
  (str (conf/read-value :database :dir) dbname))

(defn db-name
  ([type]
   (name type))
  ([type id]
   (str (name type) "_" id)))

(defn db-conn [dbname]
  (-> {:dbtype (conf/read-value :database :type)}
       (merge {:dbname (db-path dbname)})
       (jdbc/get-connection)
       (jdbc/with-options {:builder-fn rs/as-unqualified-lower-maps})))

(defn conn
  ([]
   (conn :master))
  ([db-type]
   (assert (= db-type :master))
   (db-conn (db-name db-type)))
  ([db-type id]
   (assert (not (= db-type :master)))
   (db-conn (db-name db-type id))))

