(ns source.workers.feeds
  (:require [source.util :as utils]
            [source.workers.xml-schemas :as xml]
            [source.db.honey :as hon]
            [source.rss.youtube :as yt]
            [clojure.string :as string]
            [taoensso.telemere :as t]
            [pg.core :as pg]
            [congest.jobs :as congest]))

(defn create-feed!
  "Creates feed with incoming posts pulled from RSS feed and starts associated job"
  [ds {:keys [user-id feed-metadata]}]
  (pg/with-transaction [ds ds]
    (let [{:keys [provider-id rss-url content-type-id]} feed-metadata
          datetime (utils/get-utc-timestamp-string)
          youtube? (= provider-id 1)
          rss-url (if (and youtube? (not (string/includes? rss-url "/feeds/videos.xml?channel_id=")))
                    (->> (yt/find-channel-id rss-url)
                         (str "https://www.youtube.com/feeds/videos.xml?channel_id="))
                    rss-url)
          selection-schemas (->> [:= :provider-id provider-id]
                                 (assoc {} :where)
                                 (xml/selection-schemas ds))
          latest-ss (->> selection-schemas
                         (reduce (fn [acc {:keys [id]}]
                                   (conj acc id)) [])
                         (apply max -1))
          extracted (try
                      (when-not (= latest-ss -1)
                        (xml/extract-data ds latest-ss rss-url))
                      (catch Exception e
                        (throw
                         (t/error!
                          ::data-extraction
                          (ex-info (str "Data extraction failed for feed creation - RSS feed url: " rss-url " creator-id " user-id)
                                   {:panic? "Not a huge deal, possibly just user error - but panic if it's not user error"
                                    :possible-cause "RSS feed url might be incorrect or provider is unsupported"
                                    :next-steps (str
                                                 "Check if the RSS feed url is correct. If it is, test data extraction in the admin panel with provider-id "
                                                 provider-id)
                                    :raw-error (.getMessage e)})))))
          extracted-posts (get-in extracted [:feed :posts])
          display-picture (if youtube?
                            (yt/channel-image (get-in extracted [:feed :url]))
                            (get-in extracted [:feed :display-picture]))
          new-feed (hon/insert!
                    ds
                    {:tname :feeds
                     :data (merge feed-metadata {:title (get-in extracted [:feed :title])
                                                 :display-picture display-picture
                                                 :description (get-in extracted [:feed :description])
                                                 :user-id user-id
                                                 :created-at datetime
                                                 :state "pending"})
                     :ret :1})

          _ (when (or (nil? display-picture) (= display-picture ""))
              (t/log! {:level :error
                       :msg (str "Failed to pull display picture for feed [ " (:id new-feed) " " (:title new-feed) "] provider-id " provider-id)}))

          extended-posts (mapv (fn [post]
                                 (merge post
                                        {:feed-id (:id new-feed)
                                         :creator-id user-id
                                         :content-type-id content-type-id
                                         :thumbnail (or (:thumbnail post) (:display-picture new-feed))}))
                               extracted-posts)]
      (if (some? extracted-posts)
        (do
          (hon/insert! ds {:tname :incoming-posts
                           :data extended-posts})
          new-feed)
        false))))

(defn update-feed! [ds {:keys [feed-id feed-metadata]}]
  (hon/update! ds {:tname :feeds
                   :where [:= :id feed-id]
                   :data feed-metadata
                   :ret :1}))

(defn hard-delete-feed! [ds feed-id]
  (pg/with-transaction [ds ds]
    (let [post-ids (mapv :id (hon/find ds {:tname :incoming-posts
                                           :where [:= :feed-id feed-id]}))
          event-ids (mapv :id (hon/find ds {:tname :events
                                            :where [:= :feed-id feed-id]}))]
      (hon/delete! ds {:tname :filtered-feeds
                       :where [:= :feed-id feed-id]})
      (hon/delete! ds {:tname :filtered-posts
                       :where [:in :post-id post-ids]})
      (hon/delete! ds {:tname :incoming-posts
                       :where [:= :feed-id feed-id]})
      (hon/delete! ds {:tname :feed-categories
                       :where [:= :feed-id feed-id]})
      (when (seq event-ids)
        (hon/delete! ds {:tname :event-categories
                         :where [:in :event-id event-ids]}))
      (hon/delete! ds {:tname :events
                       :where [:= :feed-id feed-id]})
      (hon/delete! ds {:tname :feeds
                       :where [:= :id feed-id]}))))

(defn deregister-feed-job! [js job-id]
  (congest/deregister! js job-id))

(defn update-feed-categories! [ds {:keys [feed-id categories]}]
  (let [update-data (mapv (fn [{:keys [id]}]
                            {:feed-id feed-id
                             :category-id id}) categories)]
    (pg/with-transaction [ds ds]
      (when (seq update-data)
        (hon/delete! ds {:tname :feed-categories
                         :where [:= :feed-id feed-id]})
        (hon/insert! ds {:tname :feed-categories
                         :data update-data
                         :ret :*})))))
