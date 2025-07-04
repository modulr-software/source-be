(ns source.services.analytics
  (:require [source.db.interface :as db]))

(defn insert-event! [ds {:keys [_values _ret] :as opts}]
  (->> {:tname :analytics}
       (merge opts)
       (db/insert! ds)))
