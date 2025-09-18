(ns source.bundle-migrations.001-init-bundle-db
  (:require [source.db.bundle]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [ds-bundle (:db-bundle context)]
    (tables/create-tables!
     ds-bundle
     :source.db.bundle
     [:outgoing-posts
      :post-heuristics
      :analytics
      :event-categories])))

(defn run-down! [context]
  (let [ds-bundle (:db-bundle context)]
    (tables/drop-tables!
     ds-bundle
     [:outgoing-posts
      :post-heuristics
      :analytics
      :event-categories])))
