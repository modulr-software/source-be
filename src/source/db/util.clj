(ns source.db.util
  (:require [source.config :as conf]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [source.db.master.outgoing-posts :as oposts]
            [source.db.master.users :as users]
            [source.db.master.bundles :as bundles]
            [jsonista.core :as json]
            [source.db.master.core :as master]
            [malli.generator :as mg]))

(def ^:private sqlite-config
  {:dbtype "sqlite"})

(defn db-path [dbname]
  (str (conf/read-value :database-dir) dbname))

;; TODO:
;; - introduce utility to generate dynamic db names

(defn conn [dbname]
  (-> sqlite-config
      (merge {:dbname (db-path (name dbname))})
      (jdbc/get-connection)
      (jdbc/with-options {:builder-fn rs/as-unqualified-lower-maps})))

(defn creator-id
  ([post-id]
   (-> (conn :master)
       (creator-id post-id)))
  ([ds post-id]
   (-> (oposts/select-outgoing-post-by-id ds {:id post-id})
       (:creator_id))))

(comment
  (db-path "master")
  (conn :master))
