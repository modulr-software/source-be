(ns source.datastore.config
  (:require [datahike.api :as d]
            [source.config :as conf]
            [clojure.string :as string]))

(defn absolute [path]
  (-> (java.io.File. ".")
      .getAbsolutePath
      (clojure.string/replace "/." "/")
      (str path)))

(defn store-path
  ([store-name]
   (-> (str (conf/read-value :database :dir) store-name)
       (absolute))))

(defn config [store-name]
  {:store {:backend :file
           :path (store-path (name store-name))}
   :schema-flexibility :read
   :initial-tx [{:db/ident        :selection-schemas/id
                 :db/valueType    :db.type/long
                 :db/cardinality  :db.cardinality/one
                 :db/unique       :db.unique/identity}
                {:db/ident        :selection-schemas/schema
                 :db/valueType    :db.type/any
                 :db/cardinality  :db.cardinality/one}

                {:db/ident        :output-schemas/id
                 :db/valueType    :db.type/long
                 :db/cardinality  :db.cardinality/one
                 :db/unique       :db.unique/identity}
                {:db/ident        :output-schemas/schema
                 :db/valueType    :db.type/any
                 :db/cardinality  :db.cardinality/one}
                {:db/ident        :output-schemas/version
                 :db/valueType    :db.type/long
                 :db/cardinality  :db.cardinality/one}

                {:db/ident        :providers/id
                 :db/valueType    :db.type/long
                 :db/cardinality  :db.cardinality/one
                 :db/unique       :db.unique/identity}
                {:db/ident        :providers/name
                 :db/valueType    :db.type/string
                 :db/cardinality  :db.cardinality/one}]})

(defn create-datastore [store-name]
  (when-not (d/database-exists? (config store-name))
    (println "Creating datastore...")
    (d/create-database (config store-name))))

(defn delete-datastore [store-name]
  (when (d/database-exists? (config store-name))
    (println "Deleting datastore...")
    (d/delete-database (config store-name))))
