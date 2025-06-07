(ns source.db.core
  (:require [source.db.bundle.analytics :as ba]
            [source.db.bundle.event-category :as bec]
            [source.db.creator.event-category :as cec]
            [source.db.creator.analytics :as ca]
            [source.db.util :as db.util]
            [source.util :as util]))

(defn setup-db [ds {:keys [table-names setup-data]}]
  (run! (fn [table-name]
          (when-let [data (get setup-data (keyword table-name))]
            (->> (keyword table-name)
                 (db.util/seed-data (:seed-data data))
                 (db.util/apply-seed ds table-name))))
        table-names))

(defn log-event [{:keys [post-id bundle-id type]}]
  (let [ds (db.util/conn :master)
        timestamp (util/get-utc-timestamp-string)
        bundle-ds (->> bundle-id
                       (db.util/db-name :bundle)
                       (db.util/conn))
        creator-ds (->> post-id
                        (db.util/creator-id ds)
                        (db.util/db-name :creator)
                        (db.util/conn))
        categories (db.util/get-post-categories bundle-ds ds post-id)]
    (let [event-id (-> (ba/insert-event bundle-ds {:post_id post-id
                                                   :event_type type
                                                   :timestamp timestamp})
                       (first))]
      (bec/insert-event-category bundle-ds {:cols ["event_id" "category_id"]
                                            :vals (mapv (fn [cat-record] [event-id (:category_id cat-record)]) categories)}))
    (let [event-id (-> (ca/insert-event creator-ds {:post_id post-id
                                                    :event_type type
                                                    :timestamp timestamp})
                       (first))]
      (cec/insert-event-category creator-ds {:cols ["event_id" "category_id"]
                                             :vals (mapv (fn [cat-record] [event-id (:category_id cat-record)]) categories)}))))
