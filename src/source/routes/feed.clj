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
                            [:user-id :int]
                            [:provider-id [:maybe :int]]
                            [:created-at :string]
                            [:updated-at [:maybe :string]]
                            [:content-type-id :int]
                            [:cadence-id :int]
                            [:baseline-id :int]
                            [:ts-and-cs [:maybe :string]]
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
                       [:cadence-id :int]
                       [:baseline-id :int]]}
   :responses  {200 {:body [:map [:message :string]]}
                401 {:body [:map [:message :string]]}
                403 {:body [:map [:message :string]]}}}

  [{:keys [ds path-params body] :as _request}]
  (services/update-feed! ds {:id (:id path-params)
                             :data body})
  (res/response {:message "successfully updated feed"}))
