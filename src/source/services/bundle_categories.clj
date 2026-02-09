(ns source.services.bundle-categories
  (:require [source.db.interface :as db]
            [source.db.bundle :as bundle]))

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
    (db/insert! ds {:tname (bundle/tname :bundle-categories bundle-id)
                    :data bundle-categories})))

(defn update-bundle-categories! [ds {:keys [bundle-id categories]}]
  (let [bundle-categories (mapv (fn [{:keys [id]}]
                                  {:bundle-id bundle-id
                                   :category-id id}) categories)]
    (db/delete! ds {:tname (bundle/tname :bundle-categories bundle-id)
                    :where [:= :bundle-id bundle-id]})
    (db/insert! ds {:tname (bundle/tname :bundle-categories bundle-id)
                    :data bundle-categories})))
