(ns source.bundle-migrations.001-init-bundle-db
  (:require [source.db.bundle]
            [source.db.tables :as tables]
            [next.jdbc :as jdbc]))

(defn run-up! [context]
  (let [ds-bundle (:db-bundle context)]
    (try (jdbc/execute! ds-bundle ["CREATE DOMAIN DATETIME TEXT"]) (catch Exception _))

    (tables/create-tables!
     ds-bundle
     :source.db.bundle
     [:outgoing-posts
      :bundle-categories
      :post-heuristics
      :analytics
      :event-categories])))

(defn run-down! [context]
  (let [ds-bundle (:db-bundle context)]
    (tables/drop-tables!
     ds-bundle
     [:outgoing-posts
      :bundle-categories
      :post-heuristics
      :analytics
      :event-categories])))
