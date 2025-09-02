(ns source.routes.feed
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

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
                            [:user-id [:maybe :int]]
                            [:provider-id [:maybe :int]]
                            [:created-at :string]
                            [:updated-at [:maybe :string]]
                            [:content-type-id :int]
                            [:cadence-id :int]
                            [:baseline-id :int]
                            [:ts-and-cs [:maybe :string]]
                            [:state [:maybe :string]]]}}}

  [{:keys [ds path-params] :as _request}]
  (-> (services/feed ds path-params)
      (res/response)))
