(ns source.db.event
  (:require [source.services.event-categories :as ec]
            [source.services.analytics :as analytics]
            [source.services.outgoing-posts :as outgoing-posts]
            [source.services.feed-categories :as feed-categories]
            [source.db.util :as db.util]
            [source.util :as util]
            [source.db.honey :as db]))

(defn get-post-categories [bundle-ds ds post-id]
  (let [feed-id (-> (outgoing-posts/outgoing-post bundle-ds {:id post-id})
                    (:feed-id))]
    (feed-categories/category-id ds {:feed-id feed-id})))

(defn log! [{:keys [post-id bundle-id type]}]
  (let [ds (db.util/conn :master)
        timestamp (util/get-utc-timestamp-string)
        bundle-ds (->> bundle-id
                       (db.util/db-name :bundle)
                       (db.util/conn))
        creator-ds (->> {:post-id post-id}
                        (db/find ds)
                        (:creator-id)
                        (db.util/db-name :creator)
                        (db.util/conn))
        categories (get-post-categories bundle-ds ds post-id)]
    (let [event-id (-> (analytics/insert-event! bundle-ds {:post_id post-id
                                                           :event_type type
                                                           :timestamp timestamp})
                       (first))]
      (ec/insert-event-category! bundle-ds {:event-id event-id
                                            :category-id (:category-id categories)}))
    (let [event-id (-> (analytics/insert-event! creator-ds {:post_id post-id
                                                            :event_type type
                                                            :timestamp timestamp})
                       (first))]
      (ec/insert-event-category! creator-ds {:event-id event-id
                                             :category-id (:category-id categories)}))))
