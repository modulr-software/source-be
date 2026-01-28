(ns source.services.content-types
  (:require [source.db.interface :as db]))

(defn content-types
  ([ds] (content-types ds {}))
  ([ds opts]
   (->> {:tname :content-types
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn content-type [ds {:keys [id where] :as opts}]
  (->> {:tname :content-types
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
