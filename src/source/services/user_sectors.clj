(ns source.services.user-sectors
  (:require [source.db.interface :as db]
            [source.db.honey :as hon]))

(defn user-sectors
  ([ds] (user-sectors ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :user-sectors
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn insert-user-sector! [ds {:keys [_data _ret] :as opts}]
  (->> {:tname :user-sectors}
       (merge opts)
       (db/insert! ds)))

(defn delete-user-sector! [ds {:keys [id where] :as opts}]
  (->> {:tname :user-sectors
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))

(defn sectors-by-user [ds {:keys [user-id where] :as _opts}]
  (hon/execute! ds
                {:select [[:user-sectors.sector-id :id] :name]
                 :from :sectors
                 :join [:user-sectors [:= :user-sectors.sector-id :sectors.id]]
                 :where (if (some? user-id)
                          [:= :user-id user-id]
                          where)}
                {:ret :*}))

(defn sector-id [ds {:keys [user-id where] :as opts}]
  (->> {:tname :user-sectors
        :where (if (some? user-id)
                 [:= :user-id user-id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

;;NEW
(defn update-user-sectors! [ds {:keys [user-id sectors]}]
  (let [update-data (reduce (fn [acc {:keys [id]}]
                              (conj acc {:user-id user-id
                                         :sector-id id})) [] sectors)]
    (delete-user-sector! ds {:where [:= :user-id user-id]})
    (insert-user-sector! ds {:data update-data})))
