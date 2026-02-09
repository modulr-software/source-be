(ns source.bundle-migrations.001-init-bundle-db
  (:require [source.db.bundle :as bundle]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [{:keys [ds-master bundle-id]} context
        tables [:outgoing-posts
                :bundle-categories
                :post-heuristics]]
    (tables/create-bundle-tables!
     ds-master
     :source.db.bundle
     tables
     bundle-id)))

(defn run-down! [context]
  (let [{:keys [ds-master bundle-id]} context]
    (tables/drop-tables!
     ds-master
     (-> [:outgoing-posts
          :bundle-categories
          :post-heuristics]
         (bundle/tnames bundle-id)))))
