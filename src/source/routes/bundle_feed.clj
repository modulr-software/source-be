(ns source.routes.bundle-feed
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.services.analytics.interface :as analytics]))

(defn get
  {:summary "Get a single RSS feed by id from RSS feeds within the uuid-authorized bundle. 
   This endpoint will update click analytics for the returned RSS feed."
   :parameters {:query [:map [:uuid {:description "Bundle UUID"} :string]]
                :path [:map [:id {:title "id"
                                  :description "Feed ID"} :int]]}
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
                            [:state [:enum "live" "not live" "pending"]]]}
                404 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id path-params] :as _request}]
  (let [feed (hon/find-one ds {:tname :feeds
                               :where [:= :id (:id path-params)]})]
    (try
      (analytics/insert-feed-click! ds feed bundle-id)
      (catch Exception e (println (.getMessage e))))
    (res/response feed)))
