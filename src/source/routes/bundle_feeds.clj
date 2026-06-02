(ns source.routes.bundle-feeds
  (:require [ring.util.response :as res]
            [source.workers.bundles :as bundles]
            [source.services.analytics.interface :as analytics]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]
            [taoensso.telemere :as t]))

(def QueryNonFiltered
  [:nonfiltered {:optional true
                 :description "Marking this field as true will disable all filters"} :boolean])

(defn post
  {:summary "Get all RSS feeds present in the bundle authorised by uuid.
   This endpoint will update impressions analytics for the returned RSS feeds."
   :description "This endpoint can be filtered by content type ID, category IDs, or most recently added feeds."
   :parameters {:query [:map
                        schemas/QueryUUID
                        schemas/QueryContentType
                        schemas/QueryLatest
                        schemas/QueryAnalytics
                        QueryNonFiltered]
                :body [:map [:category-ids [:vector :int]]]}
   :responses (-> (api/success schemas/Feeds)
                  (api/not-found))}

  [{:keys [ds bundle-id query-params body] :as _request}]
  (let [{:keys [type latest nonfiltered analytics]} query-params
        feeds (->> {:bundle-id bundle-id
                    :type type
                    :latest latest
                    :category-ids (:category-ids body)
                    :nonfiltered nonfiltered}
                   (bundles/get-outgoing-feeds ds))]
    (when (not (= analytics "false"))
      (try
        (analytics/insert-feed-impressions! ds feeds bundle-id)
        (catch Exception e (t/log! {:level :error
                                    :msg (str "Failed to insert feed impressions for bundle feeds: " (.getMessage e))}))))

    (res/response feeds)))
