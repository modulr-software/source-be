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

;;NEW
(defn insert-bundle-content-types! [ds {:keys [bundle-id content-types]}]
  (let [content-types (mapv (fn [{:keys [id]}]
                              {:bundle-id bundle-id
                               :content-type-id id}) content-types)]
    (->> {:tname :bundle-content-types
          :data content-types
          :ret :*}
         (db/insert! ds))))

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

;;NEW
(defn update-bundle-content-types! [ds {:keys [bundle-id content-types]}]
  (let [content-types (mapv (fn [{:keys [id]}]
                              {:bundle-id bundle-id
                               :content-type-id id}) content-types)]
    (delete-bundle-content-types! ds {:where [:= :bundle-id bundle-id]})
    (insert-bundle-content-types! ds {:data content-types})))
