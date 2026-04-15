(ns source.migrations.002-incoming-posts
  (:require [source.db.master]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (tables/create-tables!
     ds-master
     :source.db.master
     [:incoming-posts])))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (tables/drop-table! ds-master :incoming-posts)))
