(ns source.workers.categories
  (:require [source.db.honey :as hon]
            [pg.core :as pg]
            [source.db.util :as db.util]
            [honey.sql.helpers :as hsql]))

(defn used-categories
  "returns all categories for which feeds exist."
  [ds]
  (let [category-ids (->> (hon/find ds {:tname :feed-categories})
                          (mapv :category-id))]
    (hon/find ds {:tname :categories
                  :where (if (seq category-ids)
                           [:in :id category-ids]
                           [:= :id -1])})))

(defn delete-category! [ds category-id]
  (pg/with-transaction [ds ds]
    (let [bundle-ids (mapv :id (hon/find ds {:tname :bundles}))]
      (hon/delete! ds {:tname :feed-categories
                       :where [:= :category-id category-id]})
      (hon/delete! ds {:tname :event-categories
                       :where [:= :category-id category-id]})
      (run!
       #(hon/delete! ds (-> (db.util/tname :bundle-categories %)
                            (hsql/where [:= :category-id category-id])))
       bundle-ids)
      (hon/delete! ds {:tname :categories
                       :where [:= :id category-id]}))))

(comment
  (used-categories (db.util/conn))
  ())
