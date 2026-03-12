(ns source.routes.feed
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.workers.feeds :as feeds]
            [source.jobs.handlers :as handlers]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]))

(defn get
  {:summary "get feed by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]}
   :responses (api/success schemas/Feed)}

  [{:keys [ds path-params] :as _request}]
  (-> (hon/find-one ds {:tname :feeds
                        :where [:= :id (:id path-params)]})
      (res/response)))

(defn post
  {:summary "update feed by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]
                :body (-> (api/maybe-keys schemas/Feed)
                          (api/missoc :id :user-id :provider-id :created-at :updated-at :state :rss-url))}
   :responses  {200 {:body [:map [:message :string]]}
                401 {:body [:map [:message :string]]}
                403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params body] :as _request}]
  (feeds/update-feed! ds {:feed-id (:id path-params)
                          :feed-metadata body})
  (res/response {:message "successfully updated feed"}))

(defn delete
  {:summary "delete feed by id"
   :parameters {:path [:map [:id {:title "id"
                                  :description "feed id"} :int]]}
   :responses {200 {:body [:map [:message :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}

  [{:keys [ds js user path-params] :as _request}]
  (let [id (:id path-params)
        feed (hon/find-one ds {:tname :feeds
                               :where [:and
                                       [:= :user-id (:id user)]
                                       [:= :id id]]})
        {:keys [email]} (hon/find-one ds {:tname :users
                                          :where [:= :id (:id user)]})
        job-id (handlers/update-feed-posts-job-id email id)]
    (if (some? feed)
      (do
        (feeds/hard-delete-feed! ds id)
        (feeds/deregister-feed-job! js job-id)
        (res/response {:message "successfully deleted feed"}))
      (-> (res/response {:message "unauthorized"})
          (res/status 403)))))
