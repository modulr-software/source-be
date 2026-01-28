(ns source.services.filtered-posts
  (:require [source.db.interface :as db]))

(defn filtered-posts
  ([ds] (filtered-posts ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :filtered-posts
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))
