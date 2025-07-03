(ns source.services.businesses
  (:require [source.db.interface :as db]))

(defn businesses
  ([ds] (businesses ds {}))
  ([ds opts]
   (->> {:tname :businesses}
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
