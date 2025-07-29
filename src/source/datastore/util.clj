(ns source.datastore.util
  (:require [datahike.api :as d]
            [source.config :as conf]
            [clojure.string :as string]))

(defn absolute [path]
  (-> (java.io.File. ".")
      .getAbsolutePath
      (clojure.string/replace "/." "/")
      (str path)))

(defn store-path [store-name]
  (-> (str (conf/read-value :database :dir) store-name)
      (absolute)))

(defn config [store-name]
  {:store {:backend :file
           :path (store-path (name store-name))}
   :schema-flexibility :read})

(defn conn
  "Open a connection to a datahike store"
  [store-name]
  (d/connect (config store-name)))

(defn close
  "Close a connection to a datahike store"
  [conn]
  (d/release conn))

(defn store-name
  ([type]
   (name type))
  ([type id]
   (str (name type) "_" id)))

(comment
  (d/create-database (config :datahike))
  (d/delete-database (config :datahike))

  (defonce ds (conn :datahike))
  (d/transact ds [{:user/name "Keagan"
                   :user/age 23}])
  (d/q '{:find [?e ?n ?a]
         :where [[?e :user/name ?n]
                 [?e :user/age ?a]]}
       @ds)
  ())
