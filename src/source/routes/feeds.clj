(ns source.routes.feeds
  (:require [source.services.interface :as services]
            [source.util :as utils]
            [source.jobs.core :as jobs]
            [ring.util.response :as res]))

(defn get
  {:summary "get all feeds"
   :responses  {200 {:body [:map
                            [:users
                             [:vector
                              [:map
                               [:id :int]
                               [:title :string]
                               [:display-picture [:maybe :string]]
                               [:url [:maybe :string]]
                               [:rss-url :string]
                               [:user-id [:maybe :int]]
                               [:provider-id [:maybe :int]]
                               [:created-at :string]
                               [:updated-at [:maybe :string]]
                               [:content-type-id :int]
                               [:cadence-id :int]
                               [:baseline-id :int]
                               [:ts-and-cs [:maybe :string]]
                               [:state [:maybe :string]]]]]]}}}

  [{:keys [ds user] :as _request}]
  (-> (services/feeds ds {:where [:= :user-id (:id user)]})
      (res/response)))

(defn post
  {:summary "adds a feed and extracts data from RSS feed URL into a post and schedules a job to keep them updated"
   :parameters {:body [:map
                       [:title :string]
                       [:display-picture {:optional true} :string]
                       [:url {:optional true} :string]
                       [:rss-url :string]
                       [:provider-id :int]
                       [:content-type-id :int]
                       [:cadence-id :int]
                       [:baseline-id :int]
                       [:ts-and-cs {:optional true} :string]
                       [:state {:optional true} :string]]}
   :responses {200 {:body [:map
                           [:id :int]
                           [:title :string]
                           [:display-picture [:maybe :string]]
                           [:url [:maybe :string]]
                           [:rss-url :string]
                           [:user-id [:maybe :int]]
                           [:provider-id [:maybe :int]]
                           [:created-at :string]
                           [:updated-at [:maybe :string]]
                           [:content-type-id :int]
                           [:cadence-id :int]
                           [:baseline-id :int]
                           [:ts-and-cs [:maybe :string]]
                           [:state [:maybe :string]]]}}}

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
        new-feed (services/insert-feed!
                  ds
                  {:data (merge body {:user-id (:id user)
                                      :created-at datetime})})
        new-post (when (some? extracted)
                   (services/insert-incoming-post! ds {:data (merge extracted
                                                                    {:feed-id (:id new-feed)
                                                                     :creator-id (:id user)
                                                                     :content-type-id content-type-id})}))]

    (if (some? extracted)
      (do (jobs/register! js ds store
                          {:initial-delay (* 1000 60 60 24)
                           :auto-start true
                           :stop-after-fail false,
                           :interval (* 1000 60 60 24)
                           :recurring? true
                           :handler :update-feed-post
                           :created-at (utils/get-utc-timestamp-string)
                           :sleep false}
                          {:feed-id (:id new-feed)
                           :post-id (:id new-post)
                           :schema-id latest-ss
                           :url rss-url})
          (res/response new-feed))

      (-> (res/response {:message "Failed to extract data, did you provide a selection schema?"})
          (res/status 500)))))

(comment
  (require '[source.db.util :as db.util]
           '[source.datastore.util :as store.util]
           '[congest.jobs :as js])

  (get {:ds (db.util/conn) :user {:id 5}})
  (post {:ds (db.util/conn)
         :js (js/create-job-service [])
         :store (store.util/conn :datahike)
         :user {:id 5}
         :body {:title "primeagen test"
                :rss-url "https://www.youtube.com/feeds/videos.xml?channel_id=UCUyeluBRhGPCW4rPe_UvBZQ"
                :provider-id 1
                :content-type-id 1
                :cadence-id 1
                :baseline-id 1}})

  ())
