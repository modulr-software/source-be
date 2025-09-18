(ns source.services.post-heuristics
  (:require [source.db.interface :as db]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn insert-post-heuristics! [ds {:keys [data] :as opts}]
  (->> {:tname :post-heuristics
        :data data
        :ret :1}
       (merge opts)
       (db/insert! ds)))

(defn update-post-heuristics! [ds {:keys [id _data where] :as opts}]
  (->> {:tname :post-heuristics
        :where (if (some? id) [:= :id id] where)
        :ret :1}
       (merge opts)
       (db/update! ds)))

(defn upsert-post-heuristics! [ds {:keys [data]}]
  (hon/execute!
   ds
   (-> (hsql/insert-into :post-heuristics)
       (hsql/values data)
       (assoc :on-conflict [:post-id])
       (assoc :do-update-set {:long-heuristic :excluded.long-heuristic
                              :short-heuristic :excluded.short-heuristic}))))

(defn post-heuristics
  ([ds] (post-heuristics ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :post-heuristics
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn top-posts-by-heuristic [ds {:keys [select limit heuristic] :as _opts}]
  (hon/execute! ds
                (merge {:select (or select :*)
                        :from :post-heuristics
                        :order-by [[heuristic :desc]]
                        :limit limit})
                {:ret :*}))

(defn post-heuristic [ds {:keys [id where] :as opts}]
  (->> {:tname :post-heuristics
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
