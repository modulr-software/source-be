(ns source.workers.feeds
  (:require [source.util :as utils]
            [source.services.xml-schemas :as xml]
            [source.jobs.core :as jobs]
            [congest.jobs :as congest]
            [source.db.honey :as hon]
            [source.jobs.handlers :as handlers]))

(defn create-feed!
  "Creates feed with incoming posts pulled from RSS feed and starts associated job"
  [ds js store {:keys [user-id feed-metadata]}]
  (let [{:keys [provider-id rss-url content-type-id]} feed-metadata
        datetime (utils/get-utc-timestamp-string)
        selection-schemas (->> [:= :provider-id provider-id]
                               (assoc {} :where)
                               (xml/selection-schemas ds))
        latest-ss (->> selection-schemas
                       (reduce (fn [acc {:keys [id]}]
                                 (conj acc id)) [])
                       (apply max -1))
        extracted (when-not (= latest-ss -1)
                    (xml/extract-data store latest-ss rss-url))
        extracted-posts (get-in extracted [:feed :posts])
        new-feed (hon/insert!
                  ds
                  {:tname :feeds
                   :data (merge feed-metadata {:title (get-in extracted [:feed :title])
                                               :display-picture (get-in extracted [:feed :display-picture])
                                               :user-id user-id
                                               :created-at datetime
                                               :state "pending"})
                   :ret :1})
        extended-posts (mapv (fn [post]
                               (merge post
                                      {:feed-id (:id new-feed)
                                       :creator-id user-id
                                       :content-type-id content-type-id
                                       :thumbnail (or (:thumbnail post) (:display-picture new-feed))}))
                             extracted-posts)
        {:keys [email]} (hon/find-one ds {:tname :users
                                          :where [:= :id user-id]})]

    (if (some? extracted-posts)
      (do
        (hon/insert! ds {:tname :incoming-posts
                         :data extended-posts})

        (->> (jobs/prepare-congest-metadata
              ds
              store
              {:id (str email "-" (:id new-feed))
               :initial-delay (* 1000 60 60 24)
               :auto-start true
               :stop-after-fail false,
               :interval (* 1000 60 60 24)
               :recurring? true
               :args {:feed-id (:id new-feed)
                      :creator-id user-id
                      :content-type-id content-type-id
                      :provider-id provider-id
                      :url rss-url}
               :handler :update-feed-posts
               :created-at (utils/get-utc-timestamp-string)
               :sleep false})
             (congest/register! js))
        new-feed)
      false)))

(defn update-feed! [ds {:keys [feed-id feed-metadata]}]
  (hon/update! ds {:tname :feeds
                   :where [:= :id feed-id]
                   :data feed-metadata
                   :ret :1}))

(defn hard-delete-feed! [ds js creator-email feed-id]
  (let [job-id (handlers/update-feed-posts-job-id creator-email feed-id)
        post-ids (mapv :id (hon/find ds {:tname :incoming-posts
                                         :where [:= :feed-id feed-id]
                                         :ret :*}))]
    (hon/delete! ds {:tname :filtered-feeds
                     :where [:= :feed-id feed-id]})
    (hon/delete! ds {:tname :filtered-posts
                     :where [:in :post-id post-ids]})
    (hon/delete! ds {:tname :incoming-posts
                     :where [:= :feed-id feed-id]})
    (hon/delete! ds {:tname :feed-categories
                     :where [:= :feed-id feed-id]})
    (hon/delete! ds {:tname :events
                     :where [:= :feed-id feed-id]})
    (hon/delete! ds {:tname :feeds
                     :where [:= :id feed-id]})
    (congest/deregister! js job-id)))

(defn update-feed-categories! [ds {:keys [feed-id categories]}]
  (let [update-data (mapv (fn [{:keys [id]}]
                            {:feed-id feed-id
                             :category-id id}) categories)]
    (when (seq update-data)
      (hon/delete! ds {:tname :feed-categories
                       :where [:= :feed-id feed-id]})
      (hon/insert! ds {:tname :feed-categories
                       :data update-data
                       :ret :*}))))
