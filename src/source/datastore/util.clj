(ns source.datastore.util
  (:require [datahike.api :as d]
            [source.datastore.config :as c]))

(defn conn
  "Open a connection to a datahike store"
  [store-name]
  (d/connect (c/config store-name)))

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
  (d/create-database (c/config :datahike))
  (d/delete-database (c/config :datahike))

  (defonce ds (conn :datahike))
  (d/transact ds [{:user/name "Keagan"
                   :user/age 23}])
  (d/q '{:find [?e ?n ?a]
         :where [[?e :user/name ?n]
                 [?e :user/age ?a]]}
       @ds)
  ())
