(ns source.services.bundle-categories
  (:require [source.db.interface :as db]
            [source.db.honey :as hon]))

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

(defn categories-by-bundle [ds {:keys [bundle-id where] :as _opts}]
  (hon/execute! ds
                {:select [[:bundle-categories.category-id :id] :name]
                 :from :categories
                 :join [:bundle-categories [:= :bundle-categories.category-id :categories.id]]
                 :where (if (some? bundle-id)
                          [:= :bundle-id bundle-id]
                          where)}
                {:ret :*}))

(defn category-id [ds {:keys [bundle-id where] :as opts}]
  (->> {:tname :bundle-categories
        :where (if (some? bundle-id)
                 [:= :bundle-id bundle-id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
