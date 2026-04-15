(ns source.migrations.005-filtered-feeds-posts
  (:require [source.db.master]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (tables/create-tables!
     ds-master
     :source.db.master
     [:filtered-feeds
      :filtered-posts])))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (tables/drop-tables! 
      ds-master
      [:filtered-feeds
       :filtered-posts])))
