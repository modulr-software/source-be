(ns source.services.outgoing-posts
  (:require [source.db.interface :as db]
            [source.db.util :as db.util]))

(defn outgoing-post [ds {:keys [id where] :as opts}]
  (->> {:tname :outgoing-posts
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

