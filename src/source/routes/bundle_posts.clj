(ns source.routes.bundle-posts
  (:require [clojure.walk :as walk]
            [ring.util.response :as res]
            [source.services.analytics.interface :as analytics]
            [source.workers.bundles :as bundles]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]
            [malli.util :as mu]
            [source.logger :as logger]))

(def QueryTruncatePosts
  [:truncate
   {:optional true
    :description "Truncates text in posts to a maximum of 100 characters. Defaults to true."}
   [:enum "true" "false"]])

(def QuerySeed
  [:seed {:optional true} [:maybe :string]])

(defn post
  {:summary "Get a list of posts in the uuid-authorized bundle, determined by analytics.
   This endpoint updates impression analytics for the returned posts."
   :description "This endpoint pulls a curated list of content (determined by analytics) of the posts that made it into the bundle during post selection. This can be filtered by content type ID, category IDs, or latest (most recently added posts). If results are filtered by latest, they will not be curated by analytics.
   
   Results can be paginated using the `start` and `limit` query parameters."
   :parameters (api/params
                :body [:map [:category-ids [:vector :int]]]
                :query [:map
                        schemas/QueryUUID
                        schemas/QueryLimit
                        schemas/QueryStart
                        schemas/QueryContentType
                        schemas/QueryLatest
                        QueryTruncatePosts
                        QuerySeed])
   :responses (api/success (api/paginated [:vector (-> schemas/Post
                                                       (mu/assoc :feed-title :string))]))}

  [{:keys [ds bundle-id query-params body] :as _request}]
  (let [{:keys [limit start type latest seed truncate]} (walk/keywordize-keys query-params)
        {:keys [data] :as posts} (bundles/get-outgoing-posts
                                  ds
                                  {:bundle-id bundle-id
                                   :limit limit
                                   :start start
                                   :type type
                                   :latest latest
                                   :seed seed
                                   :truncate truncate
                                   :category-ids (:category-ids body)})]
    (try
      (analytics/insert-post-impressions! ds data bundle-id)
      (catch Exception e (logger/log-error (str "Failed to insert post impressions on bundle posts: " (.getMessage e)))))
    (res/response posts)))
