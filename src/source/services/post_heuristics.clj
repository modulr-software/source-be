(ns source.services.post-heuristics
  (:require [source.db.interface :as db]
            [source.db.honey :as hon]
            [honey.sql.helpers :as hsql]))

(defn upsert-post-heuristics! [ds {:keys [data]}]
  (hon/execute!
   ds
   (-> (hsql/insert-into :post-heuristics)
       (hsql/values data)
       (assoc :on-conflict [:post-id])
       (assoc :do-update-set {:long-heuristic :excluded.long-heuristic
                              :short-heuristic :excluded.short-heuristic}))))

(defn top-posts-by-heuristic [ds {:keys [select limit heuristic] :as _opts}]
  (hon/execute! ds
                (merge {:select (or select :*)
                        :from :post-heuristics
                        :order-by [[heuristic :desc]]
                        :limit limit})
                {:ret :*}))
