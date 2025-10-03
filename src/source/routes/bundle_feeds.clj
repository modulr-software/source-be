(ns source.routes.bundle-feeds
  (:require [ring.util.response :as res]
            [source.services.interface :as services]
            [source.db.util :as db.util]))

(defn get
  {:summary "get all feeds present in the bundle authorised by uuid"
   :parameters {:query [:map
                        [:uuid :string]
                        [:key :string]]}
   :responses {200 {:body [:vector
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
                            [:state [:enum "live" "not live" "pending"]]]]}
               404 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id] :as _request}]
  (let [bundle-ds (db.util/conn :bundle bundle-id)
        feed-ids (mapv :feed-id (services/outgoing-posts bundle-ds))]
    (res/response (services/feeds ds {:where [:in :id feed-ids]}))))
