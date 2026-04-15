(ns source.migrations.006-events
  (:require [source.db.master]
            [source.db.tables :as tables]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (tables/create-tables!
     ds-master
     :source.db.master
     [:events
      :event-categories])))
(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (tables/drop-tables!
     ds-master
     [:events
      :event-categories])))
