(ns source.db.event
  (:require [source.services.analytics.interface :as analytics]
            [source.services.feed-categories :as feed-categories]
            [source.db.util :as db.util]
            [source.util :as util]
            [source.db.honey :as db]
            [source.db.bundle :as bundle]))

(defn get-post-categories [ds post-id bundle-id]
  (let [feed-id (-> (db/find-one ds {:tname (bundle/tname :outgoing-posts bundle-id)
                                     :where [:= :id post-id]})
                    (:feed-id))]
    (feed-categories/category-id ds {:feed-id feed-id})))

(defn log! [{:keys [post-id bundle-id type]}]
  (let [ds (db.util/conn :master)
        timestamp (util/get-utc-timestamp-string)
        creator-ds (->> {:post-id post-id}
                        (db/find ds)
                        (:creator-id)
                        (db.util/db-name :creator)
                        (db.util/conn))
        categories (get-post-categories ds post-id bundle-id)]
    (let [event-id (-> (analytics/insert-event! ds {:data {:post_id post-id
                                                           :event_type type
                                                           :timestamp timestamp}
                                                    :ret :*})
                       (first))]
      (db/insert! ds {:tname (bundle/tname :event-categories bundle-id)
                      :data {:event-id event-id
                             :category-id (:category-id categories)}}))
    (let [event-id (-> (analytics/insert-event! creator-ds {:data {:post_id post-id
                                                                   :event_type type
                                                                   :timestamp timestamp}
                                                            :ret :*})
                       (first))]
      (db/insert! creator-ds {:tname :event-categories
                              :data {:event-id event-id
                                     :category-id (:category-id categories)}}))))
