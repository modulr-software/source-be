(ns source.services.bundle-categories
  (:require [source.db.interface :as db]))

(defn bundle-categories
  ([ds] (bundle-categories ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :bundle-categories
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn insert-bundle-category! [ds {:keys [_data _ret] :as opts}]
  (->> {:tname :bundle-categories}
       (merge opts)
       (db/insert! ds)))

(defn delete-bundle-category! [ds {:keys [id where] :as opts}]
  (->> {:tname :bundle-categories
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))

(defn category-id [ds {:keys [bundle-id where] :as opts}]
  (->> {:tname :bundle-categories
        :where (if (some? bundle-id)
                 [:= :bundle-id bundle-id]
                 where)
        :ret :*}
       (merge opts)
       (db/find ds)))
