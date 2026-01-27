(ns source.workers.bundles
  (:require [source.db.util :as db.util]
            [source.db.honey :as hon]))

(defn get-bundle-categories [ds bundle-id]
  (with-open [bundle-ds (db.util/conn :bundle bundle-id)]
    (let [feed-ids (->> (hon/find bundle-ds {:tname :outgoing-posts
                                             :ret :*})
                        (mapv :feed-id))
          category-ids (->> (hon/find ds {:tname :feed-categories
                                          :where [:in :feed-id feed-ids]
                                          :ret :*})
                            (mapv :category-id))]
      (hon/find ds {:tname :categories
                    :where [:in :id category-ids]
                    :ret :*}))))
