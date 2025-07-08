(ns source.db.util
  (:require [source.config :as conf]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn db-path [dbname]
  (let [db-dir (conf/read-value :database :dir)]
    (str
      db-dir
      (when (not (= (last db-dir) \/))
        "/")
      dbname)))

(defn db-name
  ([type]
   (name type))
  ([type id]
   (str (name type) "_" id)))

(defn- -conn [dbname]
  (-> {:dbtype (conf/read-value :database :type)}
       (merge {:dbname (db-path dbname)})
       (jdbc/get-connection)
       (jdbc/with-options {:builder-fn rs/as-unqualified-lower-maps})))

(defn conn
  ([]
   (conn :master))
  ([db-type]
   (assert (= db-type :master))
   (-conn (db-name db-type)))
  ([db-type id]
   (assert (or (= db-type :bundle) (= db-type :creator)))
   (-conn (db-name db-type id))))

