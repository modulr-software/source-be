(ns source.services.bundle-content-types
  (:require [source.db.interface :as db]
            [source.db.honey :as hon]))

(defn bundle-content-types
  ([ds] (bundle-content-types ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :bundle-content-types
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn insert-bundle-content-types! [ds {:keys [_data _ret] :as opts}]
  (->> {:tname :bundle-content-types}
       (merge opts)
       (db/insert! ds)))

(defn delete-bundle-content-types! [ds {:keys [id where] :as opts}]
  (->> {:tname :bundle-content-types
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))

(defn content-types-by-bundle [ds {:keys [bundle-id where] :as _opts}]
  (hon/execute! ds
                {:select [[:bundle-content-types.content-type-id :id] :name]
                 :from :content-types
                 :join [:bundle-content-types [:= :bundle-content-types.content-type-id :content-types.id]]
                 :where (if (some? bundle-id)
                          [:= :bundle-id bundle-id]
                          where)}
                {:ret :*}))

(defn content-type-id [ds {:keys [bundle-id where] :as opts}]
  (->> {:tname :bundle-content-types
        :where (if (some? bundle-id)
                 [:= :bundle-id bundle-id]
                 where)
        :ret :*}
       (merge opts)
       (db/find ds)))
