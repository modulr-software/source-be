(ns source.routes.admin-feeds
  (:require [source.services.interface :as services]
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
                             [:user-id [:maybe :int]]
                             [:provider-id [:maybe :int]]
                             [:created-at :string]
                             [:updated-at [:maybe :string]]
                             [:content-type-id :int]
                             [:cadence-id :int]
                             [:baseline-id :int]
                             [:ts-and-cs [:maybe :string]]
                             [:state [:maybe :string]]]]}}}

  [{:keys [ds] :as _request}]
    (-> (services/feeds ds)
        (res/response)))
