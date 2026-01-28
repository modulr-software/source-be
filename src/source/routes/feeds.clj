(ns source.routes.feeds
  (:require [source.workers.feeds :as feeds]
            [congest.jobs :as congest]
            [ring.util.response :as res]
            [source.db.honey :as hon]))

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

  [{:keys [js ds store user body] :as _request}]
  (let [exists (hon/exists? ds {:tname :feeds
                                :where [:= :rss-url (:rss-url body)]
                                :ret :1})]
    (if exists
      (-> (res/response {:message "There is already a feed with the given RSS feed"})
          (res/status 400))

      (let [new-feed (feeds/create-feed! ds js store {:user-id (:id user)
                                                      :feed-metadata body})]
        (if new-feed
          (res/response new-feed)
          (-> (res/response {:message "Failed to parse RSS feed"})
              (res/status 422)))))))

(comment
  (require '[source.db.util :as db.util]
           '[source.datastore.util :as store.util])

  (get {:ds (db.util/conn) :user {:id 3}})
  (post {:ds (db.util/conn)
         :js (congest/create-job-service [])
         :store (store.util/conn :datahike)
         :user {:id 3}
         :body {:rss-url "https://www.youtube.com/feeds/videos.xml?channel_id=UCUyeluBRhGPCW4rPe_UvBZQ"
                :provider-id 1
                :content-type-id 1
                :cadence-id 1
                :baseline-id 1}})

  ())
