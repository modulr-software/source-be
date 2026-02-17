(ns source.bundle-migrations.002-outgoing-posts
  (:require [source.db.bundle]
            [source.db.tables :as tables]
            [source.db.util :as db.util]))

(defn run-up! [context]
  (let [{:keys [ds-master bundle-id]} context
        tables [:outgoing-posts]]
    (tables/create-bundle-tables!
     ds-master
     :source.db.bundle
     tables
     bundle-id)))

(defn run-down! [context]
  (let [{:keys [ds-master bundle-id]} context]
    (tables/drop-tables!
     ds-master
     (mapv :tname (-> [:outgoing-posts]
                      (db.util/tnames bundle-id))))))
