(ns source.routes.bundle-posts
  (:require [clojure.walk :as walk]
            [ring.util.response :as res]
            [source.services.analytics.interface :as analytics]
            [source.workers.bundles :as bundles]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]
            [malli.util :as mu]))

(defn post
  {:summary "Get a list of posts in the uuid-authorized bundle, determined by analytics.
   This endpoint updates impression analytics for the returned posts."
   :description "This endpoint pulls a curated list of content (determined by analytics) of the posts that made it into the bundle during post selection. This can be filtered by content type ID, category IDs, or latest (most recently added posts). If results are filtered by latest, they will not be curated by analytics.
   
   Results can be paginated using the `start` and `limit` query parameters."
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
                         [:enum "true" "false"]]
                        [:seed {:optional true} [:maybe :string]]]}
   :responses (api/success (api/paginated [:vector (-> schemas/Post
                                                       (mu/assoc :feed-title :string))]))}

  [{:keys [ds bundle-id query-params body] :as _request}]
  (let [{:keys [limit start type latest seed]} (walk/keywordize-keys query-params)
        {:keys [data] :as posts} (bundles/get-outgoing-posts
                                  ds
                                  {:bundle-id bundle-id
                                   :limit limit
                                   :start start
                                   :type type
                                   :latest latest
                                   :seed seed
                                   :category-ids (:category-ids body)})]
    (try
      (analytics/insert-post-impressions! ds data bundle-id)
      (catch Exception e (println (.getMessage e))))
    (res/response posts)))
