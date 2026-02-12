(ns source.services.post-heuristics
  (:require [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]
            [source.db.util :as db.util]))

(defn upsert-post-heuristics! [ds {:keys [bundle-id data]}]
  (hon/execute!
   ds
   (-> (hsql/insert-into (:tname (db.util/tname :post-heuristics bundle-id)))
       (hsql/values data)
       (assoc :on-conflict [:post-id])
       (assoc :do-update-set {:long-heuristic :excluded.long-heuristic
                              :short-heuristic :excluded.short-heuristic}))))

(defn top-posts-by-heuristic [ds {:keys [select limit heuristic bundle-id] :as _opts}]
  (hon/execute! ds
                (merge {:select (or select :*)
                        :from (:tname (db.util/tname :post-heuristics bundle-id))
                        :order-by [[heuristic :desc]]
                        :limit limit})
                {:ret :*}))
