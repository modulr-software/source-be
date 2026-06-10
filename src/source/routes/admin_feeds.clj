(ns source.routes.admin-feeds
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.jobs.handlers :as handlers]
            [source.workers.feeds :as feeds]))

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

  [{:keys [ds] :as _request}]
  (-> (hon/find ds {:tname :feeds
                    :ret :*})
      (res/response)))

(defn delete-feed
  {:summary "delete feed by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}
               401 {:body [:map [:message :string]]}
               404 {:body [:map [:message :string]]}}}

  [{:keys [ds js path-params] :as _request}]
  (let [id (:id path-params)
        feed (hon/find-one ds {:tname :feeds
                               :where [:= :id id]})
        {:keys [email]} (hon/find-one ds {:tname :users
                                          :where [:= :id (:user-id feed)]})
        job-id (handlers/update-feed-posts-job-id email id)]
    (if (some? feed)
      (do
        (feeds/hard-delete-feed! ds id)
        (feeds/deregister-feed-job! js job-id)
        (res/response {:message "successfully deleted feed"}))
      (-> (res/response {:message "the feed with the given id does not exist"})
          (res/status 404)))))
