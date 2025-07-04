(ns source.services.event-categories
  (:require [source.db.interface :as db]))

(defn insert-event-category! [ds {:keys [_values _ret] :as opts}]
  (->> {:tname :event-categories}
       (merge opts)
       (db/insert! ds)))

