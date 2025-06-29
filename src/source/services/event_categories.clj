(ns source.services.event-categories
  (:require [source.db.interface :as db]))

(defn insert-event-category! [ds event-category]
  (->> {:tname :users
        :data event-category}
       (db/insert! ds)))

