(ns source.services.businesses
  (:require [source.db.interface :as db]))

(defn business
  [ds {:keys [id where] :as opts}]
  (->> {:tname :businesses
        :where (if id [:= :id id] where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(defn businesses
  ([ds] (businesses ds {}))
  ([ds opts]
   (->> {:tname :businesses
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn insert-business! [ds {:keys [_values _ret] :as opts}]
  (->> {:tname :businesses}
       (merge opts)
       (db/insert! ds)))

(defn update-business! [ds {:keys [id values where] :as opts}]
  (->> {:tname :businesses
        :values values
        :where (if (some? id) [:= :id id] where)}
       (merge opts)
       (db/update! ds)))

(defn delete-business! [ds {:keys [id where] :as opts}]
  (->> {:tname :businesses
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))
