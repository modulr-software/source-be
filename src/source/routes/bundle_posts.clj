(ns source.routes.bundle-posts
  (:require [clojure.walk :as walk]
            [ring.util.response :as res]
            [source.services.analytics.interface :as analytics]
            [source.workers.bundles :as bundles]))

(defn post
  {:summary "Get a list of posts in the uuid-authorized bundle, determined by analytics.
   This endpoint updates impression analytics for the returned posts."
   :parameters {:body [:map [:category-ids [:vector :int]]]
                :query [:map
                        [:uuid {:description "Bundle UUID"} :string]
                        [:limit
                         {:optional true
                          :description "Used for pagination. Specifies a number of posts to be returned."}
                         :int]
                        [:start
                         {:optional true
                          :description "Used for pagination. Specifies the starting point for the returned posts, incremented by the limit."}
                         :int]
                        [:type {:optional true
                                :description "Filters by content type ID"} :int]
                        [:latest
                         {:optional true
                          :description "Filters by most recently uploaded posts, not determined by analytics"}
                         [:enum "true" "false"]]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:post-id :string]
                            [:feed-id :int]
                            [:creator-id :int]
                            [:content-type-id :int]
                            [:title :string]
                            [:thumbnail [:maybe :string]]
                            [:info [:maybe :string]]
                            [:url [:maybe :string]]
                            [:stream-url [:maybe :string]]
                            [:season [:maybe :int]]
                            [:episode [:maybe :int]]
                            [:redacted {:optional true} [:maybe :int]]
                            [:posted-at [:maybe :string]]]]}
               404 {:boy [:map [:message :string]]}}}

  [{:keys [ds bundle-id query-params body] :as _request}]
  (let [{:keys [limit start type latest]} (walk/keywordize-keys query-params)
        posts (->> {:bundle-id bundle-id
                    :limit limit
                    :start start
                    :type type
                    :latest latest
                    :category-ids (:category-ids body)}
                   (bundles/get-outgoing-posts ds))]
    (try
      (analytics/insert-post-impressions! ds posts bundle-id)
      (catch Exception e (println (.getMessage e))))
    (res/response posts)))
