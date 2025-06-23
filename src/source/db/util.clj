(ns source.db.util
  (:require [source.config :as conf]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [source.db.bundle.outgoing-posts :as oposts]
            [source.db.master.feeds-categories :as feeds-categories]))

(defn db-path [dbname]
  (str (conf/read-value :database :dir) dbname))

(defn conn [dbname]
  (-> {:dbtype (conf/read-value :database :type)}
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

(defn db-name
  ([type]
   (name type))
  ([type id]
   (str (name type) "_" id)))

(defn get-post-categories [bundle-ds ds post-id]
  (let [feed-id (-> (oposts/select-outgoing-post-by-id bundle-ds {:id post-id})
                    (:feed_id))]
    (feeds-categories/select-by-feed-id ds {:feed-id feed-id})))


