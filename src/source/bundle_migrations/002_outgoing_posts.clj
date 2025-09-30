(ns source.bundle-migrations.002-outgoing-posts
  (:require [source.db.bundle]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [ds-bundle (:db-bundle context)]
    (tables/create-tables!
     ds-bundle
     :source.db.bundle
     [:outgoing-posts])))

(defn run-down! [context]
  (let [ds-bundle (:db-bundle context)]
    (tables/drop-tables!
     ds-bundle
     [:outgoing-posts])))
