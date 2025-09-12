(ns source.services.feed-categories
  (:require [source.db.interface :as db]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn feed-categories
  ([ds] (feed-categories ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :feed-categories
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn insert-feed-category! [ds {:keys [_data _ret] :as opts}]
  (->> {:tname :feed-categories}
       (merge opts)
       (db/insert! ds)))

(defn upsert-feed-categories! [ds {:keys [data]}]
  (hon/execute!
   ds
   (-> (hsql/insert-into :feed-categories)
       (hsql/values data)
       (assoc :on-conflict [:feed-id :category-id])
       (assoc :do-update-set {:category-id :excluded.category-id}))))

(defn delete-feed-category! [ds {:keys [id where] :as opts}]
  (->> {:tname :feed-categories
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))

(defn categories-by-feed [ds {:keys [feed-id where] :as _opts}]
  (hon/execute! ds
                {:select [[:feed-categories.category-id :id] :name]
                 :from :categories
                 :join [:feed-categories [:= :feed-categories.category-id :categories.id]]
                 :where (if (some? feed-id)
                          [:= :feed-id feed-id]
                          where)}
                {:ret :*}))

(defn category-id [ds {:keys [feed-id where] :as opts}]
  (->> {:tname :feed-categories
        :where (if (some? feed-id)
                 [:= :feed-id feed-id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))

(comment
  (require '[source.db.util :as db.util])
  (def ds (db.util/conn))

  (db/delete! ds {:tname :feed-categories})
  (feed-categories ds)
  (insert-feed-category! ds {:data {:feed-id 1
                                    :category-id 2}})
  (categories-by-feed ds {:feed-id 1})
  (upsert-feed-categories! ds {:data [{:feed-id 1
                                       :category-id 6}]})
  ())
