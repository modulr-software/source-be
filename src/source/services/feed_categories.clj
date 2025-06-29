(ns source.services.feed-categories
  (:require [source.db.interface :as db]))

(defn category-id [ds {:keys [feed-id where] :as opts}]
  (->> {:tname :feed-categories
        :where (if (some? feed-id)
                 [:= :feed-id feed-id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

