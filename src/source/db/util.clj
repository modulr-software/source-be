(ns source.db.util
  (:require [source.config :as conf]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]
            [source.db.master.outgoing-posts :as oposts]
            [source.db.master.users :as users]
            [source.db.master.bundles :as bundles]))

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

(defn db-name
  ([type]
   (name type))
  ([type id]
   (str (name type) "_" id)))

(comment
  (db-path "master")
  (conn :master)
  (let [ds (conn :master)]
    (users/create-users-table ds)
    (bundles/create-bundles-table ds)
    (oposts/create-outgoing-posts-table ds)
    (bundles/insert-bundle ds {:cols ["user_id" "hash"] :vals [1 "test"]})
    (users/insert-user ds {:email "merveillevaneck@gmail.com"
                           :password "test"
                           :firstname "merv"
                           :lastname "ilicious"
                           :business-name "modulr"
                           :type "creator"})
    (oposts/insert-outgoing-post ds {:bundle-id 1
                                     :title "Best Video"
                                     :stream-url "sdfsfd"
                                     :content-type "video"
                                     :subtitle "Test Subtitle"
                                     :creator-id 1})

    (println (creator-id ds 1))
    (users/drop-users-table ds)
    (bundles/drop-bundles-table ds)
    (oposts/drop-outgoing-posts-table ds)))
