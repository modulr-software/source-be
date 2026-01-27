(ns source.routes.feed
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.workers.feeds :as feeds]))

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
  (-> (hon/find-one ds {:tname :feeds
                        :where [:= :id (:id path-params)]})
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
                                       [:= :user-id (:id user)
                                        := :id id]]})
        {:keys [email]} (hon/find-one ds {:tname :users
                                          :where [:= :id (:id user)]})]
    (if (some? feed)
      (do
        (feeds/hard-delete-feed! ds js email id)
        (res/response {:message "successfully deleted feed"}))
      (-> (res/response {:message "unauthorized"})
          (res/status 403)))))
