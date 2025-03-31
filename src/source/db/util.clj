(ns source.db.util
  (:require [source.config :as conf]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(def ^:private sqlite-config
  {
   :dbtype "sqlite"
   })

(defn db-path [dbname]
  (str (:database-dir conf/config) "/" dbname))

(defn conn [dbname]
  (-> sqlite-config
      (merge {:dbname dbname})
      (jdbc/get-connection)
      (jdbc/with-options {:builder-fn rs/as-unqualified-lower-maps})))

(defn x
  ([q]
   (let [conn (conn "master")]
     (x conn q)))
  ([ds q]
   (jdbc/execute! ds q)))

(defn x-one
  ([q]
   (let [conn (conn "master")]
     (x-one conn q)))
  ([ds q]
   (jdbc/execute-one! ds q)))