(ns source.routes.bundle-feed
  (:require [ring.util.response :as res]
            [source.workers.bundles :as bundles]))

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
  (->> {:bundle-id bundle-id
        :feed-id (:id path-params)}
       (bundles/get-feed-in-bundle! ds)
       (res/response)))
