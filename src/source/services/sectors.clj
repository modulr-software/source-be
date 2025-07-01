(ns source.services.sectors
  (:require [source.db.interface :as db]))

(defn sectors
  ([ds] (sectors ds {}))
  ([ds opts]
   (->> {:tname :sectors}
        (merge opts)
        (db/find ds))))
