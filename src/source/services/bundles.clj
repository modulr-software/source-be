(ns source.services.bundles
  (:require [source.db.interface :as db]
            [source.util :as utils]
            [source.services.categories :as categories]
            [source.services.bundle-categories :as bundle-categories]
            [source.db.util :as db.util]))

(defn insert-bundle! [ds {:keys [_values _ret] :as opts}]
  (->> {:tname :bundles}
       (merge opts)
       (db/insert! ds)))

;;NEW
(defn create-bundle! [ds {:keys [user-id bundle-metadata]}]
  (insert-bundle! ds {:data (merge {:user-id user-id
                                    :content-type-id 1 ; temporarily assign garbo id
                                    :uuid (utils/uuid)}
                                   bundle-metadata)
                      :ret :1}))

(defn update-bundle! [ds {:keys [id data where] :as opts}]
  (->> {:tname :bundles
        :values data
        :where (if (some? id) [:= :id id] where)
        :ret :1}
       (merge opts)
       (db/update! ds)))

(defn bundles
  ([ds] (bundles ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :bundles
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn bundle [ds {:keys [id where] :as opts}]
  (->> {:tname :bundles
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(defn delete-bundle! [ds {:keys [id where] :as opts}]
  (->> {:tname :bundles
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))

(defn categories-in-bundle [ds bundle-id]
  (with-open [bundle-ds (db.util/conn :bundle bundle-id)]
    (let [category-ids (bundle-categories/category-id bundle-ds {:bundle-id bundle-id})
          id-vec (mapv (fn [{:keys [category-id]}] category-id) category-ids)]
      (categories/categories ds {:where [:in :id id-vec]}))))
