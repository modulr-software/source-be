(ns source.services.filtered-feeds
  (:require [source.db.interface :as db]))

(defn filtered-feeds
  ([ds] (filtered-feeds ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :filtered-feeds
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))
