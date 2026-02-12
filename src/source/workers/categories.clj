(ns source.workers.categories
  (:require [source.db.honey :as hon]))

(defn used-categories
  "returns all categories for which feeds exist."
  [ds]
  (let [category-ids (->> (hon/find ds {:tname :feed-categories})
                          (mapv :category-id))]
    (hon/find ds {:tname :categories
                  :where (if (seq category-ids)
                           [:in :id category-ids]
                           [:= :id -1])})))

(comment
  (require '[source.db.util :as db.util])

  (used-categories (db.util/conn))
  ())
