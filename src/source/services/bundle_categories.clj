(ns source.services.bundle-categories
  (:require [source.db.interface :as db]
            [source.db.bundle :as bundle]
            [source.db.util :as db.util]
            [honey.sql.helpers :as hsql]
            [pg.core :as pg]))

(defn category-id [ds {:keys [bundle-id where] :as opts}]
  (->> {:tname :bundle-categories
        :where (if (some? bundle-id)
                 [:= :bundle-id bundle-id]
                 where)
        :ret :*}
       (merge opts)
       (db/find ds)))

(defn insert-bundle-categories! [ds {:keys [bundle-id categories]}]
  (let [bundle-categories (mapv (fn [{:keys [id]}]
                                  {:bundle-id bundle-id
                                   :category-id id}) categories)]
    (db/insert! ds (-> (db.util/tname :bundle-categories bundle-id)
                       (assoc :data bundle-categories)))))

(defn update-bundle-categories! [ds {:keys [bundle-id categories]}]
  (pg/with-transaction [ds ds]
    (let [bundle-categories (mapv (fn [{:keys [id]}]
                                    {:bundle-id bundle-id
                                     :category-id id}) categories)]
      (db/delete! ds (-> (db.util/tname :bundle-categories bundle-id)
                         (hsql/where [:= :bundle-id bundle-id])))
      (db/insert! ds (-> (db.util/tname :bundle-categories bundle-id)
                         (assoc :data bundle-categories))))))
