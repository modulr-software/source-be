(ns source.services.cadences
  (:require [source.db.interface :as db]))

(defn cadences
  ([ds] (cadences ds {}))
  ([ds opts]
   (->> {:tname :cadences
         :ret :*}
        (merge opts)
        (db/find ds))))
