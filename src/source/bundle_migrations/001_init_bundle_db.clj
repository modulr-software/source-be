(ns source.bundle-migrations.001-init-bundle-db
  (:require [source.db.bundle :as bundle]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [{:keys [ds-master bundle-id]} context]
    (tables/create-tables!
     ds-master
     :source.db.bundle
     (-> [:outgoing-posts
          :bundle-categories
          :post-heuristics
          :analytics
          :event-categories]
         (bundle/tnames bundle-id)))))

(defn run-down! [context]
  (let [{:keys [ds-master bundle-id]} context]
    (tables/drop-tables!
     ds-master
     (-> [:outgoing-posts
          :bundle-categories
          :post-heuristics
          :analytics
          :event-categories]
         (bundle/tnames bundle-id)))))
