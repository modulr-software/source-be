(ns source.services.integration-categories
  (:require [source.db.interface :as db]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn integration-categories
  ([ds] (integration-categories ds {}))
  ([ds {:keys [where] :as opts}]
   (->> {:tname :integration-categories
         :where where
         :ret :*}
        (merge opts)
        (db/find ds))))

(defn insert-integration-category! [ds {:keys [_data _ret] :as opts}]
  (->> {:tname :integration-categories}
       (merge opts)
       (db/insert! ds)))

(defn delete-integration-category! [ds {:keys [id where] :as opts}]
  (->> {:tname :integration-categories
        :where (if (some? id)
                 [:= :id id]
                 where)
        :ret :1}
       (merge opts)
       (db/delete! ds)))

(defn categories-by-integration [ds {:keys [integration-id where] :as _opts}]
  (hon/execute! ds
                {:select [[:integration-categories.category-id :id] :name]
                 :from :categories
                 :join [:integration-categories [:= :integration-categories.category-id :categories.id]]
                 :where (if (some? integration-id)
                          [:= :integration-id integration-id]
                          where)}
                {:ret :*}))

(defn category-id [ds {:keys [integration-id where] :as opts}]
  (->> {:tname :integration-categories
        :where (if (some? integration-id)
                 [:= :integration-id integration-id]
                 where)
        :ret :1}
       (merge opts)
       (db/find ds)))
