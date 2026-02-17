(ns source.routes.feeds
  (:require [source.workers.feeds :as feeds]
            [congest.jobs :as congest]
            [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.util :as util]
            [source.jobs.core :as jobs]))

(defn get
  {:summary "get all feeds"
   :responses  {200 {:body [:vector
                            [:map
                             [:id :int]
                             [:title :string]
                             [:display-picture [:maybe :string]]
                             [:url [:maybe :string]]
                             [:rss-url :string]
                             [:user-id :int]
                             [:provider-id [:maybe :int]]
                             [:created-at :string]
                             [:updated-at [:maybe :string]]
                             [:content-type-id :int]
                             [:cadence-id :int]
                             [:baseline-id :int]
                             [:ts-and-cs [:maybe :int]]
                             [:state [:enum "live" "not live" "pending"]]]]}}}

  [{:keys [ds user] :as _request}]
  (-> (hon/find ds {:tname :feeds
                    :where [:= :user-id (:id user)]})
      (res/response)))

(defn post
  {:summary "adds a feed and extracts data from RSS feed URL to create incoming posts and schedules a job to keep them updated"
   :parameters {:body [:map
                       [:display-picture {:optional true} :string]
                       [:url {:optional true} :string]
                       [:rss-url :string]
                       [:provider-id :int]
                       [:content-type-id :int]
                       [:cadence-id :int]
                       [:baseline-id :int]
                       [:ts-and-cs {:optional true} :int]]}
   :responses {200 {:body [:map
                           [:id :int]
                           [:title :string]
                           [:display-picture [:maybe :string]]
                           [:url [:maybe :string]]
                           [:rss-url :string]
                           [:user-id :int]
                           [:provider-id [:maybe :int]]
                           [:created-at :string]
                           [:updated-at [:maybe :string]]
                           [:content-type-id :int]
                           [:cadence-id :int]
                           [:baseline-id :int]
                           [:ts-and-cs [:maybe :int]]
                           [:state [:enum "live" "not live" "pending"]]]}}}

  [{:keys [js ds user body] :as _request}]
  (let [exists (hon/exists? ds {:tname :feeds
                                :where [:= :rss-url (:rss-url body)]
                                :ret :1})]
    (if exists
      (-> (res/response {:message "There is already a feed with the given RSS feed"})
          (res/status 400))

      (let [{:keys [provider-id rss-url content-type-id]} body
            new-feed (feeds/create-feed! ds {:user-id (:id user)
                                             :feed-metadata body})
            {:keys [email]} (hon/find-one ds {:tname :users
                                              :where [:= :id (:id user)]})]
        (if new-feed
          (do
            ;TODO: service needed
            (->> (jobs/prepare-congest-metadata
                  ds
                  {:id (str email "-" (:id new-feed))
                   :initial-delay (* 1000 60 60 24)
                   :auto-start true
                   :stop-after-fail false,
                   :interval (* 1000 60 60 24)
                   :recurring? true
                   :args {:feed-id (:id new-feed)
                          :creator-id (:id user)
                          :content-type-id content-type-id
                          :provider-id provider-id
                          :url rss-url}
                   :handler :update-feed-posts
                   :created-at (util/get-utc-timestamp-string)
                   :sleep false})
                 (congest/register! js))
            (res/response new-feed))

          (-> (res/response {:message "Failed to parse RSS feed"})
              (res/status 422)))))))

(comment
  (require '[source.db.util :as db.util])

  (get {:ds (db.util/conn) :user {:id 3}})
  (post {:ds (db.util/conn)
         :js (congest/create-job-service [])
         :user {:id 3}
         :body {:rss-url "https://www.youtube.com/feeds/videos.xml?channel_id=UCUyeluBRhGPCW4rPe_UvBZQ"
                :provider-id 1
                :content-type-id 1
                :cadence-id 1
                :baseline-id 1}})

  ())
