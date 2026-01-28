(ns source.services.baselines
  (:require [source.db.interface :as db]))

(defn baselines
  ([ds] (baselines ds {}))
  ([ds opts]
   (->> {:tname :baselines
         :ret :*}
        (merge opts)
        (db/find ds))))
