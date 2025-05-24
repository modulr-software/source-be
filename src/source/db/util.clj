(ns source.db.util
  (:require [source.config :as conf]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(def ^:private sqlite-config
  {:dbtype "sqlite"})

(defn db-path [dbname]
  (str (conf/read-value :database-dir) "/" dbname))

;; TODO:
;; - introduce utility to generate dynamic db names

(defn conn [dbname]
  (-> sqlite-config
      (merge {:dbname (db-path dbname)})
      (jdbc/get-connection)
      (jdbc/with-options {:builder-fn rs/as-unqualified-lower-maps})))
