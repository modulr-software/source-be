(ns source.services.providers
  (:require [source.db.interface :as db]))

(defn provider [ds {:keys [id where] :as opts}]
  (->> {:tname :providers
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
