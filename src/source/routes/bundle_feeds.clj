(ns source.routes.bundle-feeds
  (:require [ring.util.response :as res]
            [source.services.interface :as services]
            [source.db.util :as db.util]
            [clojure.walk :as walk]
            [honey.sql.helpers :as hsql]))

(defn post
  {:summary "get all feeds present in the bundle authorised by uuid"
   :parameters {:query [:map
                        [:uuid :string]
                        [:type {:optional true} :int]
                        [:latest {:optional true} :boolean]
                        [:nonfiltered {:optional true} :boolean]]
                :body [:map [:category-ids [:vector :int]]]}
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

  [{:keys [ds bundle-id query-params body] :as _request}]
  (let [bundle-ds (db.util/conn :bundle bundle-id)
        {:keys [category-ids]} body
        {:keys [type latest nonfiltered]} (walk/keywordize-keys query-params)
        feed-ids (mapv :feed-id (services/outgoing-posts bundle-ds))
        category-filtered-feed-ids (if (empty? category-ids)
                                     feed-ids
                                     (->> (hsql/where
                                           [:in :feed-id feed-ids]
                                           [:in :category-id category-ids])
                                          (services/feed-categories ds)
                                          (mapv :feed-id)))
        blocked-feed-ids (if (some? nonfiltered)
                           []
                           (mapv :feed-id (services/filtered-feeds ds {:where [:= :bundle-id bundle-id]})))
        query (-> (when type [:= :content-type-id type])
                  (hsql/where [:in :id category-filtered-feed-ids]
                              [:not [:in :id blocked-feed-ids]])
                  (hsql/order-by (when latest [:created-at :desc])))
        type-filtered (services/feeds ds query)]

    (res/response type-filtered)))
