(ns source.routes.bundle-feeds
  (:require [ring.util.response :as res]
            [source.db.util :as db.util]
            [source.workers.bundles :as bundles]
            [source.services.analytics.interface :as analytics]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]
            [source.logger :as logger]))

(defn post
  {:summary "Get all RSS feeds present in the bundle authorised by uuid.
   This endpoint will update impressions analytics for the returned RSS feeds."
   :description "This endpoint can be filtered by content type ID, category IDs, or most recently added feeds."
   :parameters {:query [:map
                        [:uuid {:description "Bundle UUID"} :string]
                        [:type {:optional true
                                :description "Filters by content type ID"} :int]
                        [:latest {:optional true
                                  :description "Filters by most recently uploaded feeds"} :boolean]
                        [:nonfiltered {:optional true
                                       :description "Marking this field as true will disable all filters"} :boolean]]
                :body [:map [:category-ids [:vector :int]]]}
   :responses (-> (api/success schemas/Feeds)
                  (api/not-found))}

  [{:keys [ds bundle-id query-params body] :as _request}]
  (let [{:keys [type latest nonfiltered]} query-params
        feeds (->> {:bundle-id bundle-id
                    :type type
                    :latest latest
                    :category-ids (:category-ids body)
                    :nonfiltered nonfiltered}
                   (bundles/get-outgoing-feeds ds))]
    (try
      (analytics/insert-feed-impressions! ds feeds bundle-id)
      (catch Exception e (logger/log-error (str "Failed to insert feed impressions for bundle feeds: " (.getMessage e)))))
    (res/response feeds)))

(comment
  (def ds (db.util/conn :master))

  ())
