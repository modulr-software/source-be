(ns source.bundle-migrations.002-outgoing-posts
  (:require [source.db.bundle :as bundle]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [{:keys [ds-master bundle-id]} context
        tables [:outgoing-posts]]
    (tables/create-tables!
     ds-master
     :source.db.bundle
     tables
     (-> tables
         (bundle/tnames bundle-id)))))

(defn run-down! [context]
  (let [{:keys [ds-master bundle-id]} context]
    (tables/drop-tables!
     ds-master
     (-> [:outgoing-posts]
         (bundle/tnames bundle-id)))))
