(ns source.services.analytics
  (:require [source.db.interface :as db]))

; this needs to be updated to return the added event as well
(defn insert-event! [ds event]
  (->> {:tname :analytics
        :data event}
       (db/insert! ds)))
