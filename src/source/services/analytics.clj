(ns source.services.analytics
  (:require [source.db.interface :as db]))

; TODO: this needs to return the added event as well
(defn insert-event! [ds event]
  (->> {:tname :analytics
        :data event}
       (db/insert! ds)))
