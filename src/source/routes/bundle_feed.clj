(ns source.routes.bundle-feed
  (:require [source.services.interface :as services]
            [ring.util.response :as res]
            [source.services.analytics.interface :as analytics]))

(defn get
  {:summary "get feed by id"
   :parameters {:query [:map [:uuid :string]]
                :path [:map [:id {:title "id"
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
                            [:state [:enum "live" "not live" "pending"]]]}
                404 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id path-params] :as _request}]
  (let [feed (services/feed ds path-params)]
    (analytics/insert-feed-click ds feed bundle-id)
    (res/response feed)))
