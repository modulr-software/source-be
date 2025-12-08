(ns source.routes.feed
  (:require [source.services.interface :as services]
            [ring.util.response :as res]
            [congest.jobs :as congest]
            [source.jobs.handlers :as handlers]
            [source.services.analytics.interface :as analytics]))

(defn get
  {:summary "get feed by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]}
   :responses  {200 {:body [:map
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

  [{:keys [ds path-params] :as _request}]
  (-> (services/feed ds path-params)
      (res/response)))

(defn post
  {:summary "update feed by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]
                :body [:map
                       [:title :string]
                       [:display-picture {:optional true} :string]
                       [:url {:optional true} :string]
                       [:ts-and-cs {:optional true} :int]
                       [:cadence-id :int]
                       [:baseline-id :int]]}
   :responses  {200 {:body [:map [:message :string]]}
                401 {:body [:map [:message :string]]}
                403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params body] :as _request}]
  (services/update-feed! ds {:id (:id path-params)
                             :data body})
  (res/response {:message "successfully updated feed"}))

(defn hard-delete-feed! [ds js creator-email feed-id]
  (let [job-id (handlers/update-feed-posts-job-id creator-email feed-id)
        post-ids (mapv :id (services/incoming-posts ds {:where [:= :feed-id feed-id]}))]
    (services/delete-filtered-feed! ds {:where [:= :feed-id feed-id]})
    (services/delete-filtered-post! ds {:where [:in :post-id post-ids]})
    (services/delete-incoming-post! ds {:where [:= :feed-id feed-id]})
    (services/delete-feed-category! ds {:where [:= :feed-id feed-id]})
    (analytics/delete-event! ds {:where [:= :feed-id feed-id]})
    (services/delete-feed! ds {:where [:= :id feed-id]})
    (congest/deregister! js job-id)))

(defn delete
  {:summary "delete feed by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds js user path-params] :as _request}]
  (let [id (:id path-params)
        {:keys [email]} (services/user ds {:id (:id user)})]
    (hard-delete-feed! ds js email id)
    (res/response {:message "successfully deleted feed"})))
