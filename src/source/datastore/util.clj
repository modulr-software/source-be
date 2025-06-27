(ns source.datastore.util
  (:require [datalevin.core :as d]
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

(defn conn
  "Open a connection to a datalevin kv store"
  [store-name] (d/open-kv (store-path store-name)))

(defn close
  "Close a connection to a datalevin store"
  [conn]
  (d/close-kv conn))
