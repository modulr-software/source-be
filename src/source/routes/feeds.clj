(ns source.routes.feeds
  (:require [source.services.interface :as services]
            [source.util :as utils]
            [congest.jobs :as congest]
            [source.jobs.core :as jobs]
            [ring.util.response :as res]))

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
  (-> (services/feeds ds {:where [:= :user-id (:id user)]})
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
  (let [{:keys [provider-id rss-url content-type-id]} body
        datetime (utils/get-utc-timestamp-string)
        selection-schemas (->> [:= :provider-id provider-id]
                               (assoc {} :where)
                               (services/selection-schemas ds))
        latest-ss (->> selection-schemas
                       (reduce (fn [acc {:keys [id]}]
                                 (conj acc id)) [])
                       (apply max -1))
        extracted (when-not (= latest-ss -1)
                    (services/extract-data store {:schema-id latest-ss
                                                  :url rss-url}))
        extracted-posts (get-in extracted [:feed :posts])
        new-feed (services/insert-feed!
                  ds
                  {:data (merge body {:title (get-in extracted [:feed :title])
                                      :user-id (:id user)
                                      :created-at datetime
                                      :state "pending"})})
        extended-posts (mapv (fn [post]
                               (merge post
                                      {:feed-id (:id new-feed)
                                       :creator-id (:id user)
                                       :content-type-id content-type-id})) extracted-posts)
        {:keys [email]} (services/user ds {:id (:id user)})]

    (if (some? extracted-posts)
      (do
        (services/insert-incoming-post! ds {:data extended-posts})

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
                      :creator-id (:id user)
                      :content-type-id content-type-id
                      :provider-id provider-id
                      :url rss-url}
               :handler :update-feed-posts
               :created-at (utils/get-utc-timestamp-string)
               :sleep false})
             (congest/register! js))
        (res/response new-feed))

      (-> (res/response {:message "failed to extract data"})
          (res/status 500)))))

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
