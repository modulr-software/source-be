(ns source.workers.users
  (:require [source.workers.feeds :as feeds]
            [source.workers.integrations :as integrations]
            [source.db.honey :as hon]
            [pg.core :as pg]))

(defn hard-delete-creator! [ds js user-id email]
  (let [feed-ids (mapv :id (hon/find ds {:tname :feeds
                                         :where [:= :user-id user-id]}))]
    (run! #(feeds/hard-delete-feed! ds js (str email "-" %) %) feed-ids)
    (hon/delete! ds {:tname :events
                     :where [:= :creator-id user-id]})))

(defn hard-delete-distributor! [ds js user-id]
  (let [bundle-ids (mapv :id (hon/find ds {:tname :bundles
                                           :where [:= :user-id user-id]}))]
    (run! #(integrations/hard-delete-bundle! ds js (str "bundle_" %) %) bundle-ids)
    (hon/delete! ds {:tname :events
                     :where [:= :distributor-id user-id]})))

(defn hard-delete-user! [ds js user-type user-id]
  (pg/with-transaction [ds ds]
    (let [{:keys [email business-id]} (hon/find-one ds {:tname :users
                                                        :where [:= :id user-id]})]
      (cond
        (= user-type :creator)
        (hard-delete-creator! ds js user-id email)
        (= user-type :distributor)
        (hard-delete-distributor! ds js user-id))

      (hon/delete! ds {:tname :user-sectors
                       :where [:= :user-id user-id]})
      (when (some? business-id) (hon/delete! ds {:tname :businesses
                                                 :where [:= :id business-id]}))
      (hon/delete! ds {:tname :users
                       :where [:= :id user-id]}))))

(defn soft-delete-user! [ds user-id]
  (hon/update! ds {:tname :users
                   :where [:= :id user-id]
                   :data {:removed 1}}))

(defn cancel-soft-user-deletion! [ds user-id]
  (hon/update! ds {:tname :users
                   :where [:= :id user-id]
                   :data {:removed 0}}))

(defn removed? [ds user-id]
  (let [removed? (-> (hon/find ds {:tname :users
                                   :where [:= :id user-id]})
                     (:removed))]
    (when (or (nil? removed?) (= removed? 0)) true)))
